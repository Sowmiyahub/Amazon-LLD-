class NameFilter implements SearchFilter {
    private final PathMatcher matcher;

    public NameFilter(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Invalid pattern");
        }

        matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + pattern);
    }

    @Override
    public boolean matches(Path path) {
        Path name = path.getFileName();
        return name != null && matcher.matches(name);
    }
}