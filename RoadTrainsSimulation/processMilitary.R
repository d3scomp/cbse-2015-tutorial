path <- "C:\\Users\\vlada\\git\\jdeeco-roadtrains-simulation\\RoadTrainsSimulation\\output\\"

setwd(paste(path, "Military", sep=""))

vehicles <- c(3, 5, 10, 15, 20)

variance <- list();

def=list()
eval=list() 

for(v in vehicles) {
	def[[v]] <- read.table(paste("def", v, sep="-"))
	eval[[v]] <- read.table(paste("eval", v, sep="-"))
}

for(v in vehicles) {
	def[[v]] <- def[[v]][1] + def[[v]][2]
	eval[[v]] <- eval[[v]][1] + eval[[v]][2]
}

for(v in vehicles) {
	variance <- c(variance, var(unlist(def[[v]])))
	variance <- c(variance, var(unlist(eval[[v]])))

	def[[v]] <- mean(unlist(def[[v]]))
	eval[[v]] <- mean(unlist(eval[[v]]))
}

def <- c(as.vector(unlist(def)))
eval <- c(as.vector(unlist(eval)))



win.metafile("Military.wmf", width = 8, height = 6)
par(cex=1)
par(lwd=1)
par(mar=c(4, 4, 0, 0))

plot(vehicles, def, type="b", col="red", xlab="Vehicles sharing destiantion", ylab="Total messages", xaxt="n", yaxt="n")
points(vehicles, eval, type="b", col="green")

axis(side=1, vehicles)
axis(side=2)

legend("topleft", c("Groupers without ensemble evaluation","Groupers with ensemble evaluation"), col=c("Red", "Green"),
 inset = .05, lty=c(1,1))

dev.off()
