# demo：多线程，依次打印1～xxx。（线程合作问题）
## 说明
此demo提供了多线程(基本执行单元)，依次打印1～xxx问题在不同语言下的几种方案。
___
## java：
+ 方案一： Task1  
  + 原理：
    1. 用一个变量curPos控制轮到哪个线程执行，
    2. 利用锁机制，使if (pos == curPos) {System.out.println("xxx)} 获得原子性执行的语意。（）
  + ps：即每个线程轮询：抢锁，根据curPos判断是否轮到自己，轮到自己则打印，否则释放锁，继续轮询
+ 方案二： Task2
  + 原理基本同方案一
  + 改进点：在同一把锁的同步代码区域，利用object.notify()：唤醒其他线程。object.wait()主动让出执行权、释放锁，等待其他线程唤醒。 从而减少了空轮询的次数，提高判断是否轮到自己的命中率
+ 方案三：Task3
  + 原理基本同方案二，
  + 区别：唯一区别是锁不同，用ReentrantLock代理synchronized，condition代替object
+ 方案四：Task4
  + 原理同方案三，
  + 改进点：使用多个condition，手动唤醒指定线程。从而基本消除了空轮询，线程的执行权基本和curPos的变化保持一致
+ 方案五：Task5
  + 原理：
    1. 使用多个阻塞队列来实现多线程执行权的转移，
  + 分析:
    1. 代码表达语意优于方案四，更加简洁易懂
    2. 阻塞的底层本质上和方案四相同，都是Condition.await()
    3. 根据结果，方案五性能稍差于方案四，我猜测原因如下：
       + 方案四：await(): 1.阻塞、释放锁, 2.唤醒、获取锁; 执行打印()；唤醒下一个线程()
       + 方案五：blockQueue.take(): 1.获取锁；2.阻塞、释放锁；3.唤醒、获取锁；4.释放锁；执行打印()；blockQueue.put()；blockQueue.add():5.获取锁；6释放锁
       + 方案五对于方案四：在每次循环中，多了2轮显式获取锁+释放锁的操作; 因为方案四的同步代码块是包含了整个循环，整个循环只有一次显式获取锁+释放锁的操作。
+ 方案六：Task6
  + 原理同方案五，
  + 改进点：用semaphore代替blockQueue，优化阻塞语意的实现
  
### 结果：
  性能：方案六 > 方案四 > 方案五 > 方案二 > 方案三 > 方案一

### 思考：
  1. java线程是系统线程的1:1包装，多线程代码的执行顺序，遵循java自己的happens-before原则
  2. 通常涉及多线程协作的代码，都需要有同步机制，保证协作的有序顺序。
  3. java里的同步机制一般是用锁。但是我感觉go的那套基于chan（对应java里的blockQueue）的并发编程方案在语意上更容易理解。ps：blockQueue底层也是基于锁的，有额外的性能损耗
___
## go
+ 方案一：Task
  + 原理：使用多个chan来实现多个goroutine执行权的转移，（同java的方案五）
  
### 关于goroutine的思考于分析：
  1. go的最基本执行单元不是线程，而是goroutine。在语意层面近乎等效thread。
  2. go在语言层面有一个标准的并发编程方案：使用 chan
  3. 内存使用和速度上：goroutine在创建、销毁、调度优于thread
  4. io密集计算优于thread模型：go的官方库，对很多io操作进行了异步优化，使很多io操作在goroutine阻塞，但是底层thread不阻塞（类似于用协程优化异步回调写法），有利于提高thread利用率，
  5. 在cpu使用率上：在cpu密集型计算的时候，反倒因为goroutine的调度是发生在用户态有额外的cpu运行开销

___
## python
### 说明
用python写这个功能，是为了尝试不同的编程方案。所以我这里只尝试了在协程上实现。
我这里的实现都是基于单线程调度来做，其实已经不属于这个问题了
+ 方案一：task1
  + 原理:
    + coroutine + 基于事件循环的协程调度
    + 通过await Future对象，实现协程切换
+ 方案二： task2
  + 原理：
    + 生成器 + 自定义生成器（协程）调度
    + 通过 yield 下一个待执行生成器的序号，实现生成器（协程）切换
### 关于coroutine（协程）的一些思考：
1. 在python里，thread是基本的执行单元；而coroutine要在事件循环里调用，可以把coroutine看作是事件循环的基本执行单元，即在事件循环里，协程在语意上近似为线程。
2. 同goroutine的思路一样（实际上是goroutine参考了coroutine的思路）：
   1. coroutine通过把基本执行单元调度的工作，从系统内核转移到用户层（go把这个调度放在语言底层; python则是在官方库中,给出了基于事件循环的调度方案）
3. 通过sync关键字声明coroutine，通过await关键字+可等待对象实现仅在coroutine层面的阻塞，来实现线程复用. 
4. python里有两种执行单元，thread和coroutine，在各自执行单元里相同语意的阻塞，在实现上是不同的。为里有效利用协程，所以在协程中，涉及io/阻塞的调用时，我们尽可能用支持协程(支持异步)的库。例如：
```python
import asyncio
import time

# 模拟同步io库的操作
def sync_io():
    time.sleep(5)

    
    
# 模拟支持协程(支持异步)的io库的
async def async_io():
      await asyncio.sleep(5)

async def task1():
  print('task1 start')
  sync_io()
  print('task1 end')

async def task2():
  print('task2 start')
  await async_io()
  print('task2 end')

# 执行task1 * 2， 总计时间 10s，
# 原因： 协程是由单线程调度 time.sleep(5)阻塞了线程
asyncio.run(asyncio.wait([task1(), task1()]))
# 执行task1 * 2， 总计时间 5s，
# 原因： 虽然协程是由单线程调度， await asyncio.sleep(5) 是支持协程的库，是协程层面的阻塞，不阻塞线程
asyncio.run(asyncio.wait([task1(), task1()]))

```
  
  
