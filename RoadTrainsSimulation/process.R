setwd("C:\\Users\\vlada\\git\\jdeeco-roadtrains-simulation\\RoadTrainsSimulation\\output\\")

g1 <- read.table("groupers-1")
g2 <- read.table("groupers-2")
g3 <- read.table("groupers-3")
g5 <- read.table("groupers-5")
g10 <- read.table("groupers-10")
g15 <- read.table("groupers-15")

r1 <- read.table("random-1")
r2 <- read.table("random-2")
r3 <- read.table("random-3")
r5 <- read.table("random-5")
r10 <- read.table("random-10")
r15 <- read.table("random-15")

g1 <- g1[1] + g1[2]
g2 <- g2[1] + g2[2]
g3 <- g3[1] + g3[2]
g5 <- g5[1] + g5[2]
g10 <- g10[1] + g10[2]
g15 <- g15[1] + g15[2]

g1 <- mean(unlist(g1))
g2 <- mean(unlist(g2))
g3 <- mean(unlist(g3))
g5 <- mean(unlist(g5))
g10 <- mean(unlist(g10))
g15 <- mean(unlist(g15))

r1 <- r1[1] + r1[2]
r2 <- r2[1] + r2[2]
r3 <- r3[1] + r3[2]
r5 <- r5[1] + r5[2]
r10 <- r10[1] + r10[2]
r15 <- r15[1] + r15[2]

r1 <- mean(unlist(r1))
r2 <- mean(unlist(r2))
r3 <- mean(unlist(r3))
r5 <- mean(unlist(r5))
r10 <- mean(unlist(r10))
r15 <- mean(unlist(r15))

g = c(g1, g2, g3, g5, g10, g15)
r = c(r1, r2, r3, r5, r10, r15)

num <- c(1, 2, 3, 5, 10, 15)

plot(num, r, type="b", col="red")
points(num, g, type="b", col="green")