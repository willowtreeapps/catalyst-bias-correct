# catalyst-bias-correct
A Slack Application that identifies and corrects unconscious gender bias in messages and conversations

# Introduction
The #BiasCorrect Plug-In is designed to help users fight their unconscious gender bias, by flagging it to them during real-time conversations on Slack and offering up alternative bias-free words or phrases. Think spell check, but for gender bias.

We’re releasing The #BiasCorrect Plug-In code in hopes that other people will take it and adapt it for their own platforms so that we can rid the workplace of gender bias, once and for all.

Learn more about how unconscious gender bias impacts women in the workplace and find other tools to become a catalyst for change at [Catalyst](https://catalyst.org/biascorrect).

## Setting up a development environment

### Mac

* Check Java version: `java --version`
	
	This should be Java 8+, likely already Java 12+ if you are running a recent OS.  Install a more modern Java if necessary.

* Install `sbt`: `brew install sbt`
* Run tests: `sbt test:compile`
* Run app using: `sbt run`
* Verify by navigating to http://localhost:4542
