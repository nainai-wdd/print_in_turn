import asyncio
import time


# 通过事件循环调度，多个协程依次打印
class Task1:
    def __init__(self, pos, max_pos, max, future_list):
        self.pos = pos
        self.max_pos = max_pos
        self.max = max
        self.future_list = future_list

    async def run(self):
        i = self.pos
        next_pos = (self.pos + 1) % self.max_pos
        while i <= self.max:
            await self.future_list[self.pos]
            self.future_list[self.pos] = asyncio.Future()
            print(self.pos.__str__() + "- " + i.__str__())
            i = i + self.max_pos
            self.future_list[next_pos].set_result(None)


def run_by_task1(max_pos, max):
    # 初始化等待事件列表
    future_list = []
    for i in range(max_pos):
        future_list.append(asyncio.Future())
    future_list[0].set_result(None)
    # 初始化协程
    co_list = []
    for i in range(max_pos):
        co_list.append(Task1(i, max_pos, max, future_list).run())
    co_list = asyncio.wait(co_list)
    loop = asyncio.get_event_loop()
    # 执行：通过事件循环调度
    start = time.time()
    loop.run_until_complete(co_list)
    end = time.time()
    return "async_func  spend time: " + str(end - start) + "s"


# 通过自定义调度，多个协程（生成器）依次打印
class Task2:
    def __init__(self, pos, max_pos, max, gen_list):
        self.pos = pos
        self.max_pos = max_pos
        self.max = max
        self.gen_list = gen_list

    def run(self):
        i = self.pos
        next_pos = (self.pos + 1) % self.max_pos
        while i <= self.max:
            print(self.pos.__str__() + "- " + i.__str__())
            i = i + self.max_pos
            yield next_pos
        yield -1


def run_by_task2(max_pos, max):
    # 初始化生成器
    gen_list = []
    for i in range(max_pos):
        gen_list.append(Task2(i, max_pos, max, gen_list).run())
    # 执行：自定义调度，每个生成器返回下一个待调度生成器的index
    start = time.time()
    cur_gen = gen_list[0]
    while True:
        next_gen_index = next(cur_gen)
        if next_gen_index == -1:
            break
        cur_gen = gen_list[next_gen_index]
    end = time.time()
    return "gen_func  spend time: " + str(end - start) + "s"


# 单线程同步执行
def run_by_sync(max):
    start = time.time()
    i = 0
    while i <= max:
        print(i)
        i = i + 1
    end = time.time()
    return "sync_func  spend time: " + str(end - start) + "s"


if __name__ == '__main__':
    max = 10000 # 打印数字数量
    max_pos = 20 # 协程数量
    sync_run_msg = run_by_sync(max)
    async_run_msg = run_by_task1(max_pos, max)
    gen_run_msg = run_by_task2(max_pos, max)
    print(sync_run_msg)
    print(async_run_msg)
    print(gen_run_msg)


