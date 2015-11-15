__author__ = 'avik'

# Data structures for implementing search
import heapq

class Stack:

    def __init__(self):
        self.list = []

    def isEmpty(self):
        return len(self.list) == 0

    def push(self,item):
        self.list.append(item)

    def pop(self):
        return self.list.pop()

    def sort(self):
        sorted(self.list)


class Queue:

    def __init__(self):
        self.list = []

    def isEmpty(self):
        return len(self.list) == 0

    def enqueue(self,item):
        self.list.insert(0,item)

    def dequeue(self):
        return self.list.pop()

'''
class PriorityQueue:

    def  __init__(self):
        self.list = []
        self.count = 0

    def push(self, item, priority):
        entry = (priority, self.count, item)
        heapq.heappush(self.list, entry)
        self.count += 1

    def pop(self):
        (_, _, item) = heapq.heappop(self.list)
        return item

    def display(self):
        print self.list

    def isEmpty(self):
        return len(self.list) == 0

'''

class PriorityQueue:

    def  __init__(self):
        self.list = []
        #self.count = 0

    def push(self, item, priority):
        entry = (priority, item)
        heapq.heappush(self.list, entry)
        #self.count += 1

    def pop(self):
        (_, item) = heapq.heappop(self.list)
        return item

    def display(self):
        print self.list

    def isEmpty(self):
        return len(self.list) == 0

    def removeItem(self,item):
        size = len(self.list)
        found = False
        for index in range(0,size):
            if item == self.list[index][1][0]:
                discarded = self.list[index]
                found = True
                break
        if found == True:
            self.list.remove(discarded )

    def getTotalCost(self,item):
        size = len(self.list)
        found = False
        for index in range(0,size):
            #print "index:",index,item
            if item == self.list[index][1][0]:
                cost,x = self.list[index]
                found = True
                break
        if found == True:
            return cost
