import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FindService {

    public List<Path> search(Path root, List<SearchFilter> filters)
            throws IOException {

        List<Path> results = new ArrayList<>();

        if (root == null || !Files.isDirectory(root)) {
            throw new IllegalArgumentException("Invalid directory");
        }

        if (filters == null) {
            filters = Collections.emptyList();
        }

        searchRecursively(root, filters, results);
        return results;
    }

    private void searchRecursively(
            Path path, List<SearchFilter> filters, List<Path> results)
            throws IOException {

        boolean matches = true;

        for (SearchFilter filter : filters) {
            if (!filter.matches(path)) {
                matches = false;
                break;
            }
        }

        if (matches) {
            results.add(path);
        }

        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> children =
                         Files.newDirectoryStream(path)) {

                for (Path child : children) {
                    searchRecursively(child, filters, results);
                }
            }
        }
    }
}
