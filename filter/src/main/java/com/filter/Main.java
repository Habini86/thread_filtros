package com.filter;

import java.awt.Color;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        try {
            int valor;
            Scanner scanner = new Scanner(System.in);

            try {
                System.err.println("1-Img pesada | 2-Img leve");
                valor = scanner.nextInt();

            } catch (Exception e) {
                System.err.println("Erro ao ler a entrada: " + e.getMessage());
                return;

            } finally {
                scanner.close();
            }

            // Carrega a imagem
            File inputFile = new File(valor == 1 ? "img.jpg" : "img2.jpg");
            BufferedImage imagem = ImageIO.read(inputFile);

            // Calcula o tempo de execução da edição da imagem
            long startTime = System.currentTimeMillis();
            editaImagemPixelAPixel(imagem);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Salva a imagem modificada
            File outputFile = new File("imagem_editada.jpg");
            ImageIO.write(imagem, "jpg", outputFile);

            System.out.println("Imagem editada e salva com sucesso!");
            System.out.println("Tempo de execução: " + duration + " ms");

        } catch (IOException e) {
            System.err.println("Erro ao processar imagem: " + e.getMessage());
        }
    }

    public static void editaImagemPixelAPixel(BufferedImage imagem) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        int totalPixels = largura * altura;

        // Quantidade de núcleos disponíveis (deixando 1 livre opcionalmente)
        int availableProcessors = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

        int pixelsPorThread = 1_000_000;

        // Calcular número de threads com base nos pixels
        int calculadoPorImagem = Math.max(1, totalPixels / pixelsPorThread);

        // Número final de threads: o menor entre o calculado e os núcleos disponíveis
        int numThreads = Math.min(calculadoPorImagem, availableProcessors);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                int startY = (altura / numThreads) * threadIndex;
                int endY = (threadIndex == numThreads - 1) ? altura : startY + (altura / numThreads);

                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < largura; x++) {
                        int c = imagem.getRGB(x, y);

                        Color color = new Color(c);

                        int red = color.getRed();
                        int green = color.getGreen();
                        int blue = color.getBlue();

                        red = green = blue = (red + green + blue) / 3;

                        int rgb = new Color(red, green, blue).getRGB();

                        imagem.setRGB(x, y, rgb);
                    }
                }
            });
        }

        executor.shutdown();
        try {
            // Define um tempo limite para aguardar a conclusão das threads
            if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
                // Se o tempo limite for atingido, tenta interromper as threads restantes
                executor.shutdownNow();
                // Aguarda novamente para garantir que todas as threads sejam interrompidas
                if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
                    System.err.println("As threads não terminaram!");
                }
            }
        } catch (InterruptedException e) {
            // Se a espera for interrompida, tenta interromper as threads restantes
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}