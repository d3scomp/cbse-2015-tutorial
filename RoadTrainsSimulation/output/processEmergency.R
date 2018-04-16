path <- "~/cbse-2015-tutorial/RoadTrainsSimulation/output/"

crashes <- c(1, 2, 3, 5, 10, 15, 20)

variance <- list();

for(prefix in c("111", "122")) {

	setwd(paste(path, prefix, sep=""))

	g <- list()
	r <- list()
	for(c in crashes) {
		g[[c]] <- read.table(paste(paste("groupers", c, sep="-"), "txt", sep="."))
		r[[c]] <- read.table(paste(paste("random", c, sep="-"), "txt", sep="."))
	}

	sg <- list()
	sr <- list()
	for(c in crashes) {
		#sg[[c]] <- g[[c]][1] + g[[c]][2]
		#sr[[c]] <- r[[c]][1] + r[[c]][2]
		sg[[c]] <- g[[c]][2]
		sr[[c]] <- r[[c]][2]
	}

	mg <- list()
	mr <- list()
	for(c in crashes) {
		mg[[c]] <- mean(unlist(sg[[c]]))
		mr[[c]] <- mean(unlist(sr[[c]]))

		variance <- c(variance, sd(unlist(sg[[c]])) / mg[[c]])
		variance <- c(variance, sd(unlist(sr[[c]])) / mr[[c]])
	}

	mg = c(as.vector(unlist(mg))) / 1000000
	mr = c(as.vector(unlist(mr))) / 1000000
	

	pdf(paste(prefix, ".pdf", sep=""), width = 8, height = 6)
	par(cex=1.5)
	par(lwd=2)
	par(mgp=c(1.60, 0.50, 0))
	par(mar=c(3, 2.5, 1.5, 0))
	par(bty="l")

	plot(crashes, mr, type="b", col="red", xlab="# of accident sites", ylab="# of IP messages in millions", xaxt="n", yaxt="n")
	points(crashes, mg, type="b", col="green")

	axis(side=1, crashes)
	axis(side=2)

	legend("topleft", c("Gossip","Groupers"), col=c("Red", "Green"), inset = .05, lty=c(1,1))

	dev.off()
}

print("Data deviance relative to mean")
print(sprintf("%.2f%%", 100*unlist(variance)))
