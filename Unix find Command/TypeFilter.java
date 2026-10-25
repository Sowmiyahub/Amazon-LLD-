class TypeFilter implements SearchFilter {

    private EntryType type;

    public TypeFilter(EntryType type) {
        this.type = type;
    }

    @Override
    public boolean matches(Path path) {

        if (type == EntryType.ANY) {
            return true;
        } else if (type == EntryType.FILE) {
            return Files.isRegularFile(path);
        } else {
            return Files.isDirectory(path);
        }
    }
}
