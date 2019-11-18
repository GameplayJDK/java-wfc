# jwfc (java-wfc)

An abstract java implementation of the wave function collapse (wfc) algorithm.

It is specifically designed with the simple tiled version of the algorithm in mind. 

Currently the project is work in progress and more of a proof of concept type of thing.

## Installation

For now, there are no prebuilt jar files available, so you will have to clone (or download) this repository and build
and artifact yourself locally. Then add it as a jar dependency to your project.

The project requires Java version 8.

> Open TODO:

I'm currently in the process of understanding and setting up a maven repository through GitHub Packages. I'll convert
the project to a maven project (https://www.jetbrains.com/help/idea/convert-a-regular-project-into-a-maven-project.html)
and then set up the public repository package
(https://help.github.com/en/github/managing-packages-with-github-packages/configuring-apache-maven-for-use-with-github-packages).

This might take a while for me to do, as I'm working full time.

## Example:

_(This assumes an IntelliJ IDEA artifact build was configured and used to generate a jar file in the default location)_.
Feel free to experiment with different map dimensions and types inside the `TileMapTest` class.

```bash
java -jar out/artifacts/java_wfc_jar/java-wfc.jar
:: input (width=5, height=5)
0  0  0  0  0  
0  1  1  1  0  
0  1  2  1  0  
0  1  1  1  0  
0  0  0  0  0  
:: output (width=10, height=10, periodic=false)
0  0  0  0  0  0  0  0  0  1  
0  1  1  0  0  0  0  0  1  0  
1  1  0  1  0  0  0  0  0  0  
0  1  0  1  1  1  1  0  1  1  
0  0  0  0  0  0  0  0  0  0  
0  1  1  0  0  0  0  0  0  0  
0  1  1  0  0  0  0  1  0  0  
1  0  1  1  0  1  1  0  0  0  
0  1  0  1  1  1  0  0  0  1  
1  0  0  1  2  1  0  0  0  0  
:: output (width=10, height=10, periodic=true)
0  1  0  0  1  0  0  0  0  0  
0  0  1  1  0  0  0  0  1  1  
0  1  2  1  1  1  0  1  1  1  
1  0  1  0  0  0  0  0  0  1  
0  0  0  0  0  0  1  0  0  0  
0  0  0  1  1  0  0  0  1  0  
0  1  0  1  0  0  0  0  0  0  
0  0  0  0  0  0  0  0  1  0  
1  0  1  0  0  0  0  1  0  0  
1  1  0  0  1  0  0  0  1  0  

```

I ran the test multiple times to get a result where both maps contain all possible numbers (tiles), as the `2` has a 
very low weight (of `1`) compared to the `1` (with a weight of `8`) and the `0` (having a weight of `16`) in the input 
map.

## The Algorithm

You can find the original algorithm [here](https://github.com/mxgmn/WaveFunctionCollapse) (written in C#).

## License

It's MIT.
