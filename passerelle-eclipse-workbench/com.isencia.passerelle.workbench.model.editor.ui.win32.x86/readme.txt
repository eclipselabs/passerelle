the modelrunner.txt functions but contains a bit too many bundles.

for some reason it also requires the model.editor.ui bundle in the model launch, otherwise submodels are not found.
it is probably only i.c.o. custom set repository submodel root folder where the prefs value is read and passed along in the
activator of the model.editor.ui.

this logic should move to a non-ui bundle.