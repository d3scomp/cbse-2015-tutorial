path <- "~/cbse-2015-tutorial/RoadTrainsSimulation/output/"

setwd(paste(path, "Military", sep=""))

vehicles <- c(3, 5, 10, 15, 20)

variance <- list();

def=list()
eval=list() 

for(v in vehicles) {
	def[[v]] <- read.table(paste(paste("def", v, sep="-"), "txt", sep="."), col.names=c("MANET", "IP"))
	eval[[v]] <- read.table(paste(paste("eval", v, sep="-"), "txt", sep="."), col.names=c("MANET", "IP"))
}

for(v in vehicles) {
	#def[[v]] <- def[[v]][1] + def[[v]][2]
	#eval[[v]] <- eval[[v]][1] + eval[[v]][2]
	def[[v]] <- def[[v]][2]
	eval[[v]] <- eval[[v]][2]
}

defM=list()
evalM=list() 

for(v in vehicles) {
	defM[[v]] <- mean(unlist(def[[v]]))
	evalM[[v]] <- mean(unlist(eval[[v]]))

	variance <- c(variance, sd(unlist(def[[v]])) / defM[[v]])
	variance <- c(variance, sd(unlist(eval[[v]])) / evalM[[v]])
}

defM <- c(as.vector(unlist(defM))) / 1000000
evalM <- c(as.vector(unlist(evalM))) / 1000000



evalP = list();
defP = list();
for(v in vehicles) {
	defP = c(defP, def[[v]] / 1000000)
	evalP = c(evalP, eval[[v]] / 1000000)
}


pdf("Military.pdf", width = 8, height = 6)
par(cex=1.5)
par(lwd=2)
par(mgp=c(1.60, 0.50, 0))
par(mar=c(3, 2.5, 1.5, 0))
par(bty="l")

plot(vehicles, defM, type="c", col="green", xlab="# of vehicles sharing destination", ylab="# of IP messages in millions", xaxt="n", yaxt="n")
points(vehicles, evalM, type="c", col="blue")

axis(side=1, vehicles)
axis(side=2)

legend("topleft", c("Groupers not evaluating ensemble\nmembership condition\n", "Groupers evaluating ensemble\nmembership condition\n"), col=c("Green", "Blue"),
 inset = .05, lty=c(1,1))

boxplot(add=TRUE, col="green", defP, at=vehicles, xaxt="n", yaxt="n")
boxplot(add=TRUE, col="blue", evalP, at=vehicles, xaxt="n", yaxt="n")


dev.off()

print("Data deviance relative to mean")
print(sprintf("%.2f%%", 100*unlist(variance)))

#data <- data.frame(evalM, defM, vehicles)







