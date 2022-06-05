package main

import (
	"fmt"
	"time"
)

type Task struct {
	Max      int
	Pos      int
	MaxPos   int
	ChanList []chan int
	End      chan int
}

func (task *Task) Run() {
	next := (task.Pos + 1) % task.MaxPos
	for i := task.Pos; i <= task.Max; i += task.MaxPos {
		<-task.ChanList[task.Pos]
		//fmt.Println(task.Pos, "-", i)
		fmt.Println(i)
		task.ChanList[next] <- 1
	}
	task.End <- 1
}

func RunOnce(maxPos, max int) {
	chanList := make([]chan int, maxPos)
	end := make(chan int, maxPos)
	for i := 0; i < maxPos; i++ {
		chanList[i] = make(chan int, 1)
	}
	tasks := make([]Task, maxPos)
	for i := 0; i < maxPos; i++ {
		tasks[i] = Task{
			Max:      max,
			Pos:      i,
			MaxPos:   maxPos,
			ChanList: chanList,
			End:      end,
		}
	}
	chanList[0] <- 1
	startTime := time.Now()
	for i := range tasks {
		go tasks[i].Run()
	}
	for i := 0; i < maxPos; i++ {
		<-end
	}
	endTime := time.Now()
	fmt.Println("async- spend time:", endTime.Sub(startTime))
	for _, channel := range chanList {
		close(channel)
	}
}
