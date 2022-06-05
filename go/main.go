package main

import (
	"fmt"
	"time"
)

func main() {
	max := 10000
	maxPos := 20
	syncMsg := sync(max)
	RunOnce(maxPos, max)
	fmt.Println(syncMsg)
}

func sync(max int) string {
	start := time.Now()
	for i := 0; i < max; i++ {
		//fmt.Println("sync", "-", i)
		fmt.Println(i)
	}
	end := time.Now()
	duration := end.Sub(start)
	return fmt.Sprintf("sync- spend time: %v", duration)
}
