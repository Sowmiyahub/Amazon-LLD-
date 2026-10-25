class NameFilter implements SearchFilter {

    private PathMatcher matcher;

    public NameFilter(String pattern) {
        matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + pattern);
    }

    @Override
    public boolean matches(Path path) {
        return matcher.matches(path.getFileName());
    }
}
