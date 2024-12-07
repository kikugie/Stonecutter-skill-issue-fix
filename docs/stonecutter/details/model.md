# Stonecutter data model

Stonecutter serialises parameters set by the buildscript to make them
available to external tools, such as the official IntelliJ IDEA plugin.

The data is saved in the YAML format. The provided locations are relative
to the versioned project root.

### Tree model
- Location: `build/stonecutter-cache/tree.yml`
- Documentation: [KDoc link](https://stonecutter.kikugie.dev/dokka/stonecutter/dev.kikugie.stonecutter.data.model/-branch-model)
- Access method `dev.kikugie.stonecutter.data.model.TreeModel.load(directory: Path)`

### Branch model
- Location: `{branch}/build/stonecutter-cache/branch.yml` or `build/stonecutter-cache/branch.yml` for the root branch.
- Documentation: [KDoc link](https://stonecutter.kikugie.dev/dokka/stonecutter/dev.kikugie.stonecutter.data.model/-tree-model)
- Access method `dev.kikugie.stonecutter.data.model.BranchModel.load(directory: Path)`

### Node model
- Location: `versions/{subproject}/build/stonecutter-cache/node.yml`.
- Documentation: [KDoc link](https://stonecutter.kikugie.dev/dokka/stonecutter/dev.kikugie.stonecutter.data.model/-node-model)
- Access method `dev.kikugie.stonecutter.data.model.NodeModel.load(directory: Path)`