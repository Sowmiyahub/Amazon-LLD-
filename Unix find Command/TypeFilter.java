class TypeFilter implements SearchFilter {
    private final EntryType type;

    public TypeFilter(EntryType type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public boolean matches(Path path) {
        if (type == EntryType.ANY) return true;

        if (type == EntryType.FILE) {
            return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);
        }

        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }
}