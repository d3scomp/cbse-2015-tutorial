print("Processing graphs -> PNG");

import os
import subprocess
import os.path, time


neato = "C:\\Program Files (x86)\\Graphviz2.38\\bin\\neato.exe";

for file in os.listdir("."):
    if file.endswith(".dot"):
        out = file + ".png";
        src = file;
        print(src + " -> " + out);

        process = True;

        if os.path.isfile(out):
            srcTime = os.path.getmtime(src);
            outTime = os.path.getmtime(out);
            if srcTime < outTime:
                process = False;

        if process:
            print("Processing:" + src + " -> " + out);
            subprocess.call([neato, "-n2", "-Tpng", "-o", out, src], shell=True);
        else:
            print("Already done: " + src + " -> " + out);
