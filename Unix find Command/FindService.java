public class FindService {

    public List<Path> search(
            Path root, List<SearchFilter> filters) throws IOException {

        if (root == null || !Files.isDirectory(root)) {
            throw new IllegalArgumentException("Invalid root directory");
        }

        List<Path> results = new ArrayList<>();
        List<SearchFilter> criteria = filters == null
                ? Collections.emptyList()
                : new ArrayList<>(filters);

        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

            private boolean matchesAll(Path path) {
                for (SearchFilter filter : criteria) {
                    if (!filter.matches(path)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public FileVisitResult preVisitDirectory(
                    Path dir, BasicFileAttributes attrs) {
                if (matchesAll(dir)) results.add(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(
                    Path file, BasicFileAttributes attrs) {
                if (matchesAll(file)) results.add(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(
                    Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });

        return results;
    }
}