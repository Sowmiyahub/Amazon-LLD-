class NameFilter implements SearchFilter {
    public boolean matches(Path path) {
        return path.getFileName().toString().endsWith(".java");
    }
}
