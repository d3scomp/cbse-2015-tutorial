path <- "C:\\Users\\vlada\\git\\jdeeco-roadtrains-simulation\\RoadTrainsSimulation\\output\\"

crashes <- c(1, 2, 3, 5, 10, 15, 20)

variance <- list();

for(prefix in c("111", "122")) {

	setwd(paste(path, prefix, sep=""))

	g <- list()
	r <- list()
	for(c in crashes) {
		g[[c]] <- read.table(paste("groupers", c, sep="-"))
		r[[c]] <- read.table(paste("random", c, sep="-"))
	}

	sg <- list()
	sr <- list()
	for(c in crashes) {
		sg[[c]] <- g[[c]][1] + g[[c]][2]
		sr[[c]] <- r[[c]][1] + r[[c]][2]
	}

	mg <- list()
	mr <- list()
	for(c in crashes) {
		mg[[c]] <- mean(unlist(sg[[c]]))
		mr[[c]] <- mean(unlist(sr[[c]]))

		variance <- c(variance, var(unlist(sg[[c]])))
		variance <- c(variance, var(unlist(sr[[c]])))
	}

	mg = c(as.vector(unlist(mg)))
	mr = c(as.vector(unlist(mr)))
	

	win.metafile(paste(prefix, ".wmf", sep=""), width = 8, height = 6)
	par(cex=1)
	par(lwd=1)
	par(mar=c(4, 4, 0, 0))

	plot(crashes, mr, type="b", col="red", xlab="Accident sites", ylab="Total messages", xaxt="n", yaxt="n")
	points(crashes, mg, type="b", col="green")

	axis(side=1, crashes)
	axis(side=2)

	legend("topleft", c("Gossip","Groupers"), col=c("Red", "Green"), inset = .05, lty=c(1,1))

	dev.off()
}

print("Data variance")
print(unlist(variance))