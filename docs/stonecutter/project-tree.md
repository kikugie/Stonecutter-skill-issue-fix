# Project trees

In Stonecutter all projects are organized into trees, even if it has one branch.
It allows managing multiple version sets together. If you want to know how to set them up,
check [Project Settings](/stonecutter/settings) and [Project Controller](/stonecutter/controller) pages.

## `ProjectTree`
### Property overview
#### `tree.vcs`
> Version control id for this tree.

#### `tree.current`
> Active version for this tree.

#### `tree.versions`
> All version ids registered for this tree.

#### `tree.path`
> The location of this tree on the disk.
> It matches the location of the project used in the `create()` function.

#### `tree.branches`
> Map of branch ids to branch instances.

#### `tree.nodes`
> All project nodes in this tree.

### Functionality
Project tree instance can be used as a reference to the Gradle project at its location.
You can access gradle properties, register tasks, and so on directly with it.

Project tree also implements `Iterable<ProjectBranch>` for quick traversal and 
branches can be accessed with `tree["branch_name"]`.

## `ProjectBranch`
### Property overview
#### `branch.id`
> Key of this branch in the tree. Main branch has id `""`, 
> and others correspond to the given subproject names.

#### `branch.tree`
> Reference to the tree containing this branch.

#### `branch.path`
> The location of this branch on the disk.
> For the main branch it's the same as `tree.path`.

#### `branch.versions`
> All version ids registered for this branch.

#### `branch.nodes`
> Map of names to project nodes.

### Functionality
Similar to the `ProjectTree`, branch can access the Gradle project functions,
iterate over the project nodes and find them with the `[]` operator.

## `ProjectNode`
### Property overview
#### `node.metadata`
> Returns version id for this node, which contains the project name, 
> assigned semantic version and the active status.

#### `node.branch`
> Reference to the branch containing this node.

#### `node.path`
> The location of this node on the disk.

#### `node.stonecutter`
> Stonecutter plugin for this node.
> Used to access another node's plugin.

### Functionality
`ProjectNode` can also be used as a Gradle project reference, 
but comes with extra functionality to access other nodes.

#### `node.peer()`
> Finds a node on the same branch with the given name.  
> Returns `null` if no node was found.

#### `node.sibling()`
> Finds a node with the same name on the given branch.  
> Returns `null` if no node was found.

#### `node.find()`
> Combines the functionality of `peer()` and `sibling()`.  
> Returns `null` if no node was found.