# CIserver

CIserver is a Continuous Integration (CI) server that automates the process of building, testing, and deploying software projects. This project is built using Java and Maven, and it includes various components to handle webhooks, execute build processes, and store build statuses.

## Table of Contents

- [Installation](#installation)
- [Setting Up GitHub Token](#setting-up-gitHub-token)
- [Usage](#usage)
- [Build and Test](#build-and-test)
- [Implementation](#implementation)
- [Way of Working](#way-of-working)
- [Contributions](#contributions)

## Installation

To install and set up the CIserver project, follow these steps:

1. Clone the repository:
    ```sh
    git clone https://github.com/yourusername/CIserver.git
    cd CIserver
    ```

2. Ensure you have the following requirements installed on your system:
    - Java 17
    - Maven 3.8.1 or higher

3. Install dependencies with:
    ```sh
    mvn clean install
    ```

## Setting Up GitHub Token

To allow the CIserver to interact with GitHub, you need to set up a GitHub token in your local environment. Follow these steps:

1. Generate a GitHub token with the necessary permissions:
    - Go to [GitHub Settings](https://github.com/settings/tokens)
    - Click on "Generate new token"
    - Select the required scopes (e.g., `repo`, `admin:repo_hook`)
    - Generate the token and copy it

2. Add the token to your local environment:
    - On macOS and Linux, execute the following command:        
    ```sh
    export GITHUB_TOKEN=<your_generated_token>
    ```
    - On Windows, you can set the environment variable using the Command Prompt:
    ```cmd
    setx GITHUB_TOKEN <your_generated_token>
    ```

3. Restart your terminal or run the following command to apply the changes:
    ```sh
    source ~/.bashrc  # or ~/.zshrc or ~/.bash_profile
    ```

Make sure to replace `<your_generated_token>` with the actual token you generated from GitHub.

## Usage

To run the CIserver, execute the following command:
```sh
mvn compile exec:java
```
To run ngrok, execute the following command in another terminal:
```sh
ngrok http 8080
```

The server will start and listen for incoming webhooks from GitHub. It will automatically trigger build and test processes based on the received webhooks.

The Build list can be accessed on `localhost:8080/builds`.

## Build and Test

To build the project, run:
```sh
mvn clean compile
```

To run the tests, execute:
```sh
mvn test
```

Test reports can be found in the `target/surefire-reports` directory.

## Implementation

Compilation is run through the class ProcessExecutor and is used throughout the test suite. The CI-server has been unit tested using mockservers and mock-httpresponses that emulates the live environment. The notifications are presented to the user with a servlet that serves HTML so it has been tested with unit tests that controll the HTML-page contains correct information.

## Way of working

Our group is still in the "In-use" state but we're nearing the "In-place" state. Our communication has become more fluid and we've gotten used to working together with git. More experience with eachother and imporoving our workflow will lead us to "In-place" way of working.

## Contributions

Robin Widjeback:
    - Write README
    - Build list servlet
    - DefaultGithubClient
    - Tests for BuildStatus and FileBuildStatusStore 

Adam Lihagen
    - Build detail servlet
    - Build notification servlet

Filip Hedman
    - Setup repsone POST method to GitHub
    - Add FileBuildStatusStore class
    - Add build servlets

Love Göransson 
    - Created the initial code skeleton.
    - Created the servlet for handling incoming Github webhooks.
    - Creating the code for compiling the code from java. 
    -Added code for compiling the code from java.
    - Some bug fixes. 
