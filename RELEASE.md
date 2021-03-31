# Release Process

Publishing artifacts relies on credentials stored in CircleCI,
which are uploaded and deployed to Maven Central through Sonatype.

It is **strongly** recommended CHANGELOG is updated prior to publishing.

Releasing is done by 1) Updating the version and 2) Triggering deployment
through a new TAG.

### Update the version.

This is done through a branch/PR with the version being bumped.

From `master`:

```sh
make inc-version
```

This will create and push a new branch with the minor version automatically
incremented, e.g. `1.2.3` will become `1.2.4`. Alternatively, an explicit
version can be specified:

```sh
make inc-version NEW_VERSION=1.2.7
```

The resulting branch will be named `v1.2.7_bump`. Create a PR out of this branch
and merge to `master`.

### Deploying to Sonatype/Maven Central.

Once `master` is updated with the new version (from the previous step), deployment
needs to be triggered, **explicitly** specifying the new version through a tag.

From `master`:

```sh
git tag 1.2.7 && git push --tags
```

This will trigger an additional `release` job in CircleCI which will deploy the artifacts
to Sonatype, sign them and synch them to Maven Central (the `deploy.sh` script contains
such steps).

## Troubleshooting

If deployment failed, the used git tag needs to be deleted locally and in the Github repo,
as well as the newly created version in Sonatype, if any.

Try again and good luck!
