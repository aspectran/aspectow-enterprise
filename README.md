# Aspectow Enterprise Edition

Aspectow Enterprise Edition is a comprehensive, all-in-one web application server built on the powerful Aspectran framework. It is designed for building robust, scalable, and maintainable enterprise web applications. Aspectow comes with a pre-configured environment, allowing you to focus on your application logic instead of server setup.

It leverages industry-standard technologies, including **JBoss Undertow** as its high-performance web server and servlet engine, and **Apache Jasper** (from Tomcat) as its JSP engine, ensuring full compliance with the Servlet specification.

## Key Features

- **All-in-One Solution**: A complete web application server ready to run out of the box.
- **Built on Aspectran**: Inherits all the benefits of the Aspectran framework, including its AOP and IoC capabilities.
- **High-Performance Core**: Powered by JBoss Undertow for a fast and lightweight servlet container.
- **JSP Support**: Integrated with Apache Jasper for seamless JSP development.
- **Enterprise-Ready**: Designed for building and deploying mission-critical enterprise applications.
- **Easy to Run and Manage**: Simple build process and command-line tools for server management.

## Requirements

- Java 21 or later
- Maven 3.9.4 or later

## Building from Source

Follow these steps to build Aspectow from the source code:

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/aspectran/aspectow-enterprise.git
    ```

2.  **Navigate to the project directory:**
    ```sh
    cd aspectow-enterprise
    ```

3.  **Build the project with Maven:**
    This will compile the source code and package the application.
    ```sh
    mvn clean package
    ```

## Running the Server

Once the project is built, you can start the server using the Aspectran Shell.

1.  **Navigate to the `bin` directory:**
    ```sh
    cd app/bin
    ```

2.  **Start the Aspectran Shell:**
    ```sh
    ./shell.sh
    ```
    This will launch an interactive shell for managing the server.

3.  **Access the application:**
    Once the server is running, you can access the default web application in your browser at [http://localhost:8081](http://localhost:8081).

## Contributing

We welcome contributions! If you'd like to contribute, please fork the repository and submit a pull request. For major changes, please open an issue to discuss your ideas.

## License

Aspectow Enterprise Edition is licensed under the [Apache License 2.0](LICENSE.txt).
