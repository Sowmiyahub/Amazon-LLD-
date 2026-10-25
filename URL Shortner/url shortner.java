/*
Class Design: URL Shortener

1. URLShortenerService
   - Main service that shortens long URLs.
   - Resolves short codes back to original URLs.
   - Uses a repository to store and retrieve mappings.

2. URLRepository
   - Stores short-code to long-URL mappings.
   - Uses HashMaps for lookup in both directions.

3. URLMapping
   - Represents a mapping between a short code and a long URL.
   - Stores creation time and original URL.

4. ShortCodeGenerator
   - Generates unique short codes using Base62 encoding.
   - Uses a counter to produce codes from [a-zA-Z0-9].

5. Main Flow
   - User submits a long URL.
   - Service checks whether it was shortened before.
   - Generates a unique short code and stores the mapping.
   - When the short URL is accessed, the service retrieves the original URL.

Design Pattern: Separation of Responsibilities
Data Structures: HashMap
*/

import java.util.*;

class URLShortenerService {
    private final URLRepository repository;
    private final ShortCodeGenerator codeGenerator;
    private final String domain = "https://short.ly/";

    public URLShortenerService(
            URLRepository repository,
            ShortCodeGenerator codeGenerator) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
    }

    public String shortenURL(String longURL) {
        if (longURL == null || longURL.isBlank()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        String existingCode = repository.getCodeByURL(longURL);

        if (existingCode != null) {
            return domain + existingCode;
        }

        String shortCode = codeGenerator.generateCode();
        repository.save(shortCode, longURL);

        return domain + shortCode;
    }

    public String getOriginalURL(String shortCode) {
        String longURL = repository.getURLByCode(shortCode);

        if (longURL == null) {
            throw new NoSuchElementException("Short URL not found");
        }

        return longURL;
    }
}

class URLRepository {
    private final Map<String, URLMapping> codeToMapping = new HashMap<>();
    private final Map<String, String> urlToCode = new HashMap<>();

    public String getCodeByURL(String longURL) {
        return urlToCode.get(longURL);
    }

    public String getURLByCode(String shortCode) {
        URLMapping mapping = codeToMapping.get(shortCode);
        return mapping == null ? null : mapping.getLongURL();
    }

    public void save(String shortCode, String longURL) {
        URLMapping mapping = new URLMapping(shortCode, longURL);

        codeToMapping.put(shortCode, mapping);
        urlToCode.put(longURL, shortCode);
    }
}

class URLMapping {
    private final String shortCode;
    private final String longURL;
    private final long createdAt;

    public URLMapping(String shortCode, String longURL) {
        this.shortCode = shortCode;
        this.longURL = longURL;
        this.createdAt = System.currentTimeMillis();
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getLongURL() {
        return longURL;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}

class ShortCodeGenerator {
    private static final String BASE62 =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private long counter = 100000;

    public synchronized String generateCode() {
        return encode(counter++);
    }

    private String encode(long number) {
        StringBuilder code = new StringBuilder();

        while (number > 0) {
            code.append(BASE62.charAt((int) (number % 62)));
            number /= 62;
        }

        return code.reverse().toString();
    }
}

public class Main {
    public static void main(String[] args) {
        URLRepository repository = new URLRepository();
        ShortCodeGenerator generator = new ShortCodeGenerator();

        URLShortenerService service =
                new URLShortenerService(repository, generator);

        String longURL = "https://www.example.com/products/electronics";

        String shortURL = service.shortenURL(longURL);
        System.out.println("Short URL: " + shortURL);

        String shortCode = shortURL.substring(shortURL.lastIndexOf("/") + 1);
        String originalURL = service.getOriginalURL(shortCode);

        System.out.println("Original URL: " + originalURL);
    }
}