# Manga Handler

Manga Handler é uma aplicação Java para gerenciar coleções de mangás. Este projeto utiliza Maven para gerenciar dependências e facilitar o processo de build.

## Vídeo de Explicação

Confira este vídeo no YouTube para uma explicação detalhada do código e de como utilizar o Manga Handler:

[![Assista no YouTube](https://img.youtube.com/vi/VdrfkYiryJ8/maxresdefault.jpg)](https://www.youtube.com/watch?v=VdrfkYiryJ8)

## Funcionalidades

- Adicionar novos mangás à coleção
- Listar todos os mangás na coleção
- Buscar mangás por título ou autor
- Atualizar mangás por titulo
- Remover mangás da coleção

## Pré-requisitos

- [Java 11+](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- [Maven 3.6+](https://maven.apache.org/download.cgi)

## Como executar o projeto

1. Clone o repositório:

    ```bash
    git clone https://github.com/moreira-arthur/mangahandler
    cd mangahandler
    ```

2. Compile o projeto utilizando Maven:

    ```bash
    mvn clean package 
    ```

3. Execute a aplicação:

    ```bash
    java -jar target/mangahandler-1.0-SNAPSHOT.jar 
    ```
4. Você também pode execeutar clicando no .jar que se encontra em 
   ```bash
    cd mangahandler/target 
    ```

## Estrutura do Projeto

```plaintext
book-handler
├── src
│   ├── main
│   │   ├── java
│   │   │   └── book
│   │   │       └── seuprojeto
│   │   │           ├── AddMangaTab.java
│   │   │           ├── IndexEntry.java
│   │   │           ├── Main.java
│   │   │           ├── Manga.java
│   │   │           ├── MangaHandler.java
│   │   │           ├── MangaHandlerGUI.java
│   │   │           ├── RemoveMangaTab.java
│   │   │           ├── SearchMangaTab.java
│   │   │           ├── TabModel.java
│   │   │           ├── TitleIndexEntry.java
│   │   │           ├── UpdateMangaTab.java
│   │   │           └── VisualizeMangaTab.java
│   ├── resources
│   ├── test
│   └── target
│       ├── classes
│       ├── generated-sources
│       ├── generated-test-sources
│       ├── maven-archiver
│       ├── maven-status
│       ├── test-classes
│       └── mangahandler-1.0-SNAPSHOT.jar
└── pom.xml
