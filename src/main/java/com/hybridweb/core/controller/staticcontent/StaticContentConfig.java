package com.hybridweb.core.controller.staticcontent;

import java.util.LinkedList;
import java.util.List;

public class StaticContentConfig {
    List<StaticComponent> components = new LinkedList<>();

    public void addComponent(String dir, String kind, StaticComponent.GitSpec spec) {
        components.add(new StaticComponent(dir, kind, spec));
    }

    public void addGitComponent(String dir, String kind, String gitUrl, String gitRef, String gitDir) {
        components.add(new StaticComponent(dir, kind, gitUrl, gitRef, gitDir));
    }

    public class StaticComponent {
        String dir;
        String kind;
        GitSpec spec;

        public StaticComponent(String dir, String kind, GitSpec spec) {
            this.dir = dir;
            this.kind = kind;
            this.spec = spec;
        }
        public StaticComponent(String dir, String kind, String gitUrl, String gitRef, String gitDir) {
            this.dir = dir;
            this.kind = kind;
            this.spec = new GitSpec(gitUrl, gitRef, gitDir);
        }

        public class GitSpec {
            String url;
            String ref;
            String dir;

            public GitSpec(String url, String ref, String dir) {
                this.url = url;
                this.ref = ref;
                this.dir = dir;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getRef() {
                return ref;
            }

            public void setRef(String ref) {
                this.ref = ref;
            }

            public String getDir() {
                return dir;
            }

            public void setDir(String dir) {
                this.dir = dir;
            }
        }

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public GitSpec getSpec() {
            return spec;
        }

        public void setSpec(GitSpec spec) {
            this.spec = spec;
        }
    }

    public List<StaticComponent> getComponents() {
        return components;
    }

    public void setComponents(List<StaticComponent> components) {
        this.components = components;
    }
}
