package com.ibayad.pgp;

import com.ibayad.pgp.model.EncryptionRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@SpringBootApplication
@RestController
public class PgpApplication {

	private static final String ENCRYPTION_ALGORITHM = "DES";
	private static final String CSV_DIRECTORY = "C:\\Users\\Randy Familiara\\OneDrive\\Documents\\csvfiles";
	private static final String DECRYPTED_DIRECTORY = "C:\\Users\\Randy Familiara\\OneDrive\\Documents\\csvfiles\\decrypted files";
	private static final String DECRYPTION_KEY = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
			"Version: GnuPG v1\n" +
			"\n" +
			"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\n" +
			"6UsYP69TClkeRCj662Xot0OUQq8NpfMFXtQ=\n" +
			"=J5wj\n" +
			"-----END PGP PUBLIC KEY BLOCK-----";

	public static void main(String[] args) {
		SpringApplication.run(PgpApplication.class, args);
		System.out.println(new EncryptionRequest().getKey());
	}

	@PostConstruct
	public void checkForNewCSVFile() throws IOException, InterruptedException {
		WatchService watchService = FileSystems.getDefault().newWatchService();
		Path csvDirectory = Paths.get(CSV_DIRECTORY);
		csvDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

		System.out.println("Watching directory for new CSV files...");

		while (true) {
			WatchKey key = watchService.take();

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();

				if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
					@SuppressWarnings("unchecked")
					WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
					Path csvFile = csvDirectory.resolve(pathEvent.context());

					if (isCSVFile(csvFile)) {
						System.out.println("New CSV file detected: " + csvFile.getFileName());

						// Perform decryption
						String decryptedFileName = decryptCSVFile(csvFile);

						if (decryptedFileName != null) {
							System.out.println("Decrypted file created: " + decryptedFileName);

							// Download decrypted file
							downloadFile(DECRYPTED_DIRECTORY + File.separator + decryptedFileName);
						} else {
							System.out.println("Decryption failed for file: " + csvFile.getFileName());
						}
					}
				}
			}

			key.reset();
		}
	}

	private static boolean isCSVFile(Path file) {
		String fileName = file.getFileName().toString();
		return fileName.endsWith(".csv");
	}

	private static String decryptCSVFile(Path csvFile) {
		try (InputStream encryptedInputStream = Files.newInputStream(csvFile);
			 ByteArrayOutputStream decryptedOutputStream = new ByteArrayOutputStream()) {
			// Truncate or pad the decryption key to 8 bytes
			byte[] keyBytes = new byte[8];
			byte[] originalKeyBytes = DECRYPTION_KEY.getBytes(StandardCharsets.UTF_8);
			System.arraycopy(originalKeyBytes, 0, keyBytes, 0, Math.min(originalKeyBytes.length, 8));


			// Create a SecretKeySpec from the key bytes
			SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "DES");

			// Create the cipher and initialize it for decryption
			Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

			// Create a CipherInputStream to decrypt the data
			CipherInputStream cipherInputStream = new CipherInputStream(encryptedInputStream, cipher);

			// Read encrypted data from the input stream and write decrypted data to the output stream
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
				decryptedOutputStream.write(buffer, 0, bytesRead);
			}

			// Close the streams
			cipherInputStream.close();
			encryptedInputStream.close();

			// Get the decrypted data as bytes
			byte[] decryptedData = decryptedOutputStream.toByteArray();

			// Save the decrypted data to a file
			String decryptedFileName = generateDecryptedFileName(csvFile);
			Path decryptedFilePath = Paths.get(DECRYPTED_DIRECTORY, decryptedFileName);
			Files.write(decryptedFilePath, decryptedData);

			return decryptedFileName;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	private static void downloadFile(String filePath) {
		// File download logic here
		System.out.println("Downloading file: " + filePath);
	}

	private static String generateDecryptedFileName(Path csvFile) {
		String fileName = csvFile.getFileName().toString();
		return "decrypted_" + fileName;
	}
}