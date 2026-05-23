package net.krusher.tet4j.util;

import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class MarkdownConverter {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No files to convert.");
            return;
        }

        Parser parser = Parser.builder()
                .extensions(Arrays.asList(TablesExtension.create()))
                .build();
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(Arrays.asList(TablesExtension.create()))
                .build();

        for (String fileName : args) {
            if (fileName.endsWith(".md")) {
                try {
                    File mdFile = new File(fileName);
                    if (!mdFile.exists()) {
                        System.err.println("File not found: " + fileName);
                        continue;
                    }
                    String content = new String(Files.readAllBytes(mdFile.toPath()));
                    Node document = parser.parse(content);
                    String htmlContent = renderer.render(document);

                    // Wrap the content with proper HTML structure that references external CSS
                    String html = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Document</title>
                        <link rel="stylesheet" href="assets/markdown.css">
                        <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600;700&family=Roboto+Mono&display=swap" rel="stylesheet">
                    </head>
                    <body>
                        %s
                    </body>
                    </html>
                    """.formatted(htmlContent);

                    String htmlFileName = fileName.replace(".md", ".html");
                    File htmlFile = new File(htmlFileName);
                    try (FileWriter writer = new FileWriter(htmlFile)) {
                        writer.write(html);
                    }
                    System.out.println("Converted " + fileName + " to " + htmlFileName);
                } catch (IOException e) {
                    System.err.println("Error converting " + fileName + ": " + e.getMessage());
                }
            }
        }
    }
}