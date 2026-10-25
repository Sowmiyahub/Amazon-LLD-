class TypeFilter implements SearchFilter {
    public boolean matches(Path path) {
        return Files.isRegularFile(path);
    }
}
