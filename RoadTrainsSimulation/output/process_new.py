from pathlib import Path
from matplotlib import pyplot as plt
import numpy as np
import scipy.optimize

def parse(directory, prefix, crashes):
	x = []
	y = []
	data = {}

	for c in crashes:
		data[c] = []
		with open(directory / Path(f"{prefix}-{c}.txt")) as file:
			for line in file:
				ip_msgs = int(line.split()[1])

				x.append(c)
				y.append(ip_msgs)
				data[c].append(ip_msgs)

	return x, y, data


def expfunc(t, a, b):
	return a * np.exp(b * t)


def processMilitary(directory):
	crashes = [3, 5, 10, 15, 20]
	dx, dy, ddata = parse(directory, "def", crashes)
	ex, ey, edata = parse(directory, "eval", crashes)

	fig = plt.figure()
	ax = fig.subplots()



	lx = np.linspace(min(crashes)- 1, max(crashes) + 1, 1000)

	popt, cov = scipy.optimize.curve_fit(expfunc, dx, dy)
	ax.plot(lx, expfunc(lx, *popt), color="red", linestyle="dotted", alpha=0.5, label="Exponential trend")

	popt, cov = scipy.optimize.curve_fit(expfunc, ex, ey)
	ax.plot(lx, expfunc(lx, *popt), color="green", linestyle="dotted", alpha=0.5, label="Exponential trend")

	# lim = ax.get_xlim()
	# for k, v in edata.items():
	# 	bp = ax.boxplot(v, positions=[k], widths=[0.35])
	# 	plt.setp(bp['boxes'], color="green")
	# 	plt.setp(bp['whiskers'], color="green")
	# 	plt.setp(bp['caps'], color="green")
	# 	plt.setp(bp['fliers'], color="green", marker="+")
	# ax.set_xlim(lim)

	ax.scatter(dx, dy, color="red", marker="v", alpha=1, label="Groupers not evaluating membership condition")
	ax.scatter(ex, ey, color="green", marker="^", alpha=1, label="Groupers evaluating membership condition")

	ax.set_xlabel("Number of vehicles sharing destination")
	ax.set_ylabel("Number of IP messages")
	ax.legend()
	fig.tight_layout()
	fig.savefig(f"{directory}.pdf")


def processEmergency(directory):
	crashes = [1, 3, 5, 10, 15, 20]
	gx, gy, gdata = parse(directory, "groupers", crashes)
	rx, ry, rdata = parse(directory, "random", crashes)

	fig = plt.figure()
	ax = fig.subplots()

	lx = np.linspace(min(crashes)- 1, max(crashes) + 1, 1000)

	gp = np.poly1d(np.polyfit(gx, gy, deg=1))
	ax.plot(lx, gp(lx), color="green", linestyle="dotted", alpha=0.5, label="Linear trend")
	ax.scatter(gx, gy, color="green", marker="^", alpha=1, label="Groupers")


	popt, cov = scipy.optimize.curve_fit(expfunc, rx, ry)
	ax.plot(lx, expfunc(lx, *popt), color="red", linestyle="dotted", alpha=0.5, label="Exponential trend")
	ax.scatter(rx, ry, color="red", marker="v", alpha=1, label="Gossip")


	ax.set_xlabel("Number of accident sites")
	ax.set_ylabel("Number of IP messages")
	ax.legend()
	fig.tight_layout()
	fig.savefig(f"{directory}.pdf")





for directory in ["111", "122"]:
	processEmergency(Path(directory))

processMilitary(Path("Military"))