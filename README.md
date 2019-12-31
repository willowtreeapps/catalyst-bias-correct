# catalyst-bias-correct
A Slack Application that identifies and corrects unconscious gender bias in messages and conversations

# Introduction
The #BiasCorrect Plug-In is designed to help users fight their unconscious gender bias, by flagging it to them during real-time conversations on Slack and offering up alternative bias-free words or phrases. Think spell check, but for gender bias.

We’re releasing The #BiasCorrect Plug-In code in hopes that other people will take it and adapt it for their own platforms so that we can rid the workplace of gender bias, once and for all.

Learn more about how unconscious gender bias impacts women in the workplace and find other tools to become a catalyst for change at [Catalyst](https://catalyst.org/biascorrect).


# Manual docker deployment

In your local workspace, create the image and push to your docker repository:
- Run `sbt docker:publishLocal` to create the catalyst-bias-correct:1.0 docker image
- Tag the image to be pushed to your repo `docker tag catalyst-bias-correct:1.0  <repo path>:catalyst-bias-correct-1.0`
- Push the image `docker push <repo path>:catalyst-bias-correct-1.0`

Verify that your repository now has the new docker image.

In your remote instance, pull the docker image and run with specified ports and environment file:
- Pull the docker image `docker pull <repo path>:catalyst-bias-correct-2.0`
- Create a .env file containing all the environment variables for the service.
- Run the docker image passing the .env file to the docker container `docker run -p 80:9000 -p 6379:6379 --env-file <absolute path to your .env file> <repo path>:catalyst-bias-correct-1.0`
