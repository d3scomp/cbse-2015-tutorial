path <- "C:\\Users\\vlada\\git\\jdeeco-roadtrains-simulation\\RoadTrainsSimulation\\output.emery\\"

setwd(paste(path, "Military", sep=""))

vehicles <- c(3, 5, 10, 15, 20)

variance <- list();

def=list()
eval=list() 

for(v in vehicles) {
	def[[v]] <- read.table(paste(paste("def", v, sep="-"), "txt", sep="."))
	eval[[v]] <- read.table(paste(paste("eval", v, sep="-"), "txt", sep="."))
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

	variance <- c(variance, var(unlist(def[[v]])) / defM)
	variance <- c(variance, var(unlist(eval[[v]])) / evalM)
}

defM <- c(as.vector(unlist(defM)))
evalM <- c(as.vector(unlist(evalM)))



win.metafile("Military.wmf", width = 8, height = 6)
par(cex=1.5)
par(lwd=2)
par(mgp=c(1.60, 0.50, 0))
par(mar=c(3, 2.5, 1.5, 0))

plot(vehicles, defM, type="b", col="red", xlab="Vehicles sharing destiantion", ylab="Total IP messages", xaxt="n", yaxt="n")
points(vehicles, evalM, type="b", col="green")

axis(side=1, vehicles)
axis(side=2)

legend("topleft", c("Groupers without ensemble evaluation","Groupers with ensemble evaluation"), col=c("Red", "Green"),
 inset = .05, lty=c(1,1))

dev.off()

print("Data variance relative to mean")
print(format(unlist(variance), digits=2, nsmall=2))
print(sprintf("%.2f%%", 100*unlist(variance)))