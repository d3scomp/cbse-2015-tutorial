path <- "C:\\Users\\vlada\\git\\jdeeco-roadtrains-simulation\\RoadTrainsSimulation\\output\\"

for(prefix in c("111", "122")) {

setwd(paste(path, prefix, sep=""))

g1 <- read.table("groupers-1")
g2 <- read.table("groupers-2")
g3 <- read.table("groupers-3")
g5 <- read.table("groupers-5")
g10 <- read.table("groupers-10")
g15 <- read.table("groupers-15")
g20 <- read.table("groupers-20")

r1 <- read.table("random-1")
r2 <- read.table("random-2")
r3 <- read.table("random-3")
r5 <- read.table("random-5")
r10 <- read.table("random-10")
r15 <- read.table("random-15")
r20 <- read.table("random-20")


g1 <- g1[1] + g1[2]
g2 <- g2[1] + g2[2]
g3 <- g3[1] + g3[2]
g5 <- g5[1] + g5[2]
g10 <- g10[1] + g10[2]
g15 <- g15[1] + g15[2]
g20 <- g20[1] + g20[2]

g1 <- mean(unlist(g1))
g2 <- mean(unlist(g2))
g3 <- mean(unlist(g3))
g5 <- mean(unlist(g5))
g10 <- mean(unlist(g10))
g15 <- mean(unlist(g15))
g20 <- mean(unlist(g20))

r1 <- r1[1] + r1[2]
r2 <- r2[1] + r2[2]
r3 <- r3[1] + r3[2]
r5 <- r5[1] + r5[2]
r10 <- r10[1] + r10[2]
r15 <- r15[1] + r15[2]
r20 <- r20[1] + r20[2]


r1 <- mean(unlist(r1))
r2 <- mean(unlist(r2))
r3 <- mean(unlist(r3))
r5 <- mean(unlist(r5))
r10 <- mean(unlist(r10))
r15 <- mean(unlist(r15))
r20 <- mean(unlist(r20))

g = c(g1, g2, g3, g5, g10, g15, g20)
r = c(r1, r2, r3, r5, r10, r15, r20)

num <- c(1, 2, 3, 5, 10, 15, 20)



win.metafile(paste(prefix, ".wmf", sep=""), width = 8, height = 6)


par(cex=1)
par(lwd=1)
par(mar=c(4, 4, 0, 0))

plot(num, r, type="b", col="red", xlab="Accident sites", ylab="Total messages", xaxt="n", yaxt="n")
points(num, g, type="b", col="green")

axis(side=1, c(1,2,3,5,10,15,20))
axis(side=2)

legend("topleft", c("Gossip","Groupers"), col=c("Red", "Green"),
 inset = .05, lty=c(1,1))

dev.off()

}