import java.awt.Color;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Filtro {
    public static void main(String[] args) {
        try {
            // Carrega a imagem
            File inputFile = new File("img.jpg");
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

        int availableProcessors = Runtime.getRuntime().availableProcessors() - 1;
        int numThreads = Math.max(1, (int) (availableProcessors * 1.5));
        // Ajuste conforme necessário

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
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                // Se o tempo limite for atingido, tenta interromper as threads restantes
                executor.shutdownNow();
                // Aguarda novamente para garantir que todas as threads sejam interrompidas
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
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