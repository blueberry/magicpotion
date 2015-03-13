
# Introduction #

Magic Potion is an ordinary Clojure (Java) library and should normally be used as a jar file that you put in your project's classpath. This quickstart guide is provided as a shortcut for trying Magic Potion if you do not have any experience with Clojure or Java, or do not have time to set up a Clojure development environment in your operating system.
The only tools that you'll need to quickly try Magic Potion are
[Mercurial](http://mercurial.selenic.com/) and [Maven](http://maven.apache.org).

# Obtaining Magic Potion #
Please note that Magic Potion is still in an early development phase. Therefore, we still do not provide downloads, nor place the library in public maven repositories. It is still easy to try Magic Potion - [Maven can help you build it from the source](Quickstart#Maven.md) with little to no effort.

# Building From The Source And Trying in REPL Console #

  1. Install Mercurial, a distributed source control management tool which you are going to use to obtain the fresh source copy of Magic Potion project:
    * Go to http://mercurial.selenic.com/
    * Click the download button to begin installation.
    * Mercurial is used from the console or through a third-party graphical user interface. The console command is `hg`. type it and you will get the list of frequently used commands `hg clone`, `hg commit` etc
  1. Install Maven, a tool for building and managing Java-based projects:
    * Go to http://maven.apache.org/download.html
    * Follow the installation steps for your operating system provided in the lower part of Maven's download page.
    * Maven console command is `mvn`
    * Make sure that Maven is properly installed. If you are behind a proxy, follow [Maven's instruction for configuring proxy settings](http://maven.apache.org/guides/mini/guide-proxies.html)
  1. Get a local copy of Magic Potion project (includes Clojure and all required dependencies) from the repository
    * Type this command in the conspole to clone Magic Potion repository: `hg clone https://magicpotion.googlecode.com/hg/ magicpotion`
    * The local copy will appear in the folder in which you executed the previous command
  1. Start Clojure REPL (interactive console) with Magic Potion in the classpath
    * be sure that, in the console, you are located in the root folder of the Magic Potion copy that you cloned (for example, home/documents/projects/magicpotion)
    * type `mvn install` in the console. magic Potion is now built and magicpotion jar is in your local Maven repository, ready to be used by other projects.
    * type `mvn clojure:repl` in the console

Congratulations, you have started the interactive prompt with everything that you need to try MagicPotion. Now, you can start writing some code.
First, you need to import Magic Potion. Type
```
(use 'org.uncomplicate.magicpotion)
```

Create a property: type

```
(property human-name [string?])
```

Create a concept: type

```
(concept person [human-name])
```

Create an individual person: type

```
(person ::human-name "Jessica")
```

It is the right time to continue to [Magic Potion Basic Tutorial](BasicTutorial.md).