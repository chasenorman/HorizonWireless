# Instructions

1.  Install Java. 
2.  Insert the .in files into the "inputs" directory (multiple can be run simultaneously!)
3.  In the main directory, run:

java -jar HorizonWireless.jar

4.  Wait, and watch solutions and their respective scores be printed to the console.
5.  If solutions slow down, use control-C to stop the execution and restart the program (this resorts edges and polls randomness.)
6.  All outputs will be visible "outputs" folder.


# Other Notes
1. java -jar HorizonWireless.jar settle

This command tries small adjustments to the current best solutions.

2. java -jar HorizonWireless.jar replace

This command tries edge-swap operations on the current best solutions.

Just because a thread prints "DONE" does not imply the solution is necessarily optimal, nor that it will fail to find better solutions if ran again. 

If you no longer want it to run a particular input, simply put its .out file into the OPT directory. This informs the program that it is "optimal" and shouldn't be pursued further.
