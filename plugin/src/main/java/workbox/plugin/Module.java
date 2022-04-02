package workbox.plugin;

import java.util.List;

class Module {
    String name;
    List<Repository> repositories;
    List<Lib> libs;

    @Override
    public String toString() {
        return "Module{" +
                "name='" + name + '\'' +
                ", repositories=" + repositories +
                ", libs=" + libs +
                '}';
    }
}
