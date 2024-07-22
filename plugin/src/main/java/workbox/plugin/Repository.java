package workbox.plugin;

import java.io.File;
import java.util.Objects;
import java.util.Set;

class Repository {
    String name;
    String url; //flatDir url = null
    Set<String> dirs;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Repository that = (Repository) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return "Repository{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", dirs=" + dirs +
                '}';
    }
}
