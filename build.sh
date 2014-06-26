#!/bin/bash

THEMES="red blue green purple orange"
LIBS="android-support-v7-appcompat google-play-services_lib"


SRC_PATH=$(cd `dirname "${BASH_SOURCE[0]}"` && pwd -P)
BASE_OUT_PATH="$(pwd -P)/output/recipes"
BIN_OUT_PATH="$BASE_OUT_PATH/apk"
SRC_OUT_PATH="$BASE_OUT_PATH/source"
DOC_OUT_PATH="$BASE_OUT_PATH/documentation"

echo -n "Cleaning old output... "
rm -rf "$BASE_OUT_PATH"
mkdir -p "$BASE_OUT_PATH"
mkdir -p "$BIN_OUT_PATH"
mkdir -p "$SRC_OUT_PATH"
mkdir -p "$DOC_OUT_PATH"
echo "done"

function clean_artifacts() {
    echo -n "Cleaning build artifacts... "
    rm -rf `find "$BASE_OUT_PATH" -name "bin"`
    rm -rf `find "$BASE_OUT_PATH" -name "gen"`
    rm -rf `find "$BASE_OUT_PATH" -name "libs-src"`
    rm -rf `find "$BASE_OUT_PATH" -name "local.properties"`
    rm -rf `find "$BASE_OUT_PATH" -name "*.jar.properties"`
    rm -rf `find "$BASE_OUT_PATH" -name "ic_*-web.png"`
    rm -rf `find "$BASE_OUT_PATH" -name "*.mech*"`
    rm -rf `find "$BASE_OUT_PATH" -name ".gitkeep"`
    rm -rf `find "$BASE_OUT_PATH" -type d -name "proguard"`
    rm -rf `find "$BASE_OUT_PATH" -name "*~"`
    echo "done"
}


(

cd "$SRC_OUT_PATH"

for lib in $LIBS; do
    echo -n "Copying library $lib... "
    cp -r "$SRC_PATH/$lib" "."
    echo "done"
done

for theme in $THEMES; do
    echo -n "Generating theme variant $theme... "
    THEME_PATH="$(pwd)/recipes-$theme"

    mkdir -p "$THEME_PATH"
    
    cp -r "$SRC_PATH/recipes-theme-$theme/"* "$THEME_PATH"
    cp -r "$SRC_PATH/recipes/"* "$THEME_PATH"
    cp -r "$SRC_PATH/recipes/.settings/" "$THEME_PATH"
    cp -r "$SRC_PATH/recipes-theme-$theme/.classpath" "$THEME_PATH"
    cp -r "$SRC_PATH/recipes-theme-$theme/.project" "$THEME_PATH"
    
    cp -r "$THEME_PATH/src-gen/"* "$THEME_PATH/src"
    rm -rf "$THEME_PATH/src-gen"
    
    package=`xmllint --xpath 'string(//manifest/@package)' "$THEME_PATH/AndroidManifest.xml"`
    sed -i "s/\(package=\"${package//./\.}\)\"/\1.$theme\"/g" "$THEME_PATH/AndroidManifest.xml"
    find "$THEME_PATH/src" -type f -name '*.java' -exec sed -i "s/\(import ${package//./\.}\)\.R;/\1.$theme.R;/g" {} \;
    sed -i "s/\(<string name=\"search_suggestions_authority\" translatable=\"false\">${package//./\.}\)/\1.$theme/g" "$THEME_PATH/res/values/config.xml"
    sed -i "s/\(<string name=\"recipes_content_authority\" translatable=\"false\">${package//./\.}\)/\1.$theme/g" "$THEME_PATH/res/values/config.xml"
    sed -i "s/\(<string name=\"app_name\">Recipes\)/\1 $(tr '[:lower:]' '[:upper:]' <<< ${theme:0:1})${theme:1}/g" "$THEME_PATH/res/values/strings.xml"
    sed -i "s/\(<project name=\"Recipes\)/\1$(tr '[:lower:]' '[:upper:]' <<< ${theme:0:1})${theme:1}/g" "$THEME_PATH/build.xml"
    sed -i 's/\(source\.dir=\).*/\1src/' "$THEME_PATH/project.properties"
    sed -i '/\.\.\/recipes-theme-red/d' "$THEME_PATH/project.properties"
    sed -i '/src-gen/d' "$THEME_PATH/.classpath"
    sed -i 's/\(<name>recipes-\)theme-/\1/' "$THEME_PATH/.project"
    
    echo "done"
done

clean_artifacts

)

(
    echo -n "building documentation... "
    bfdocs "$SRC_PATH/doc/manifest.json" "$DOC_OUT_PATH" > /dev/null
    # first time crashes so we run it second time
    bfdocs "$SRC_PATH/doc/manifest.json" "$DOC_OUT_PATH" > /dev/null
    wkhtmltopdf "$DOC_OUT_PATH/index.html" "$DOC_OUT_PATH/../documentation.pdf" > /dev/null
    echo "done"
)

(
cd "$SRC_OUT_PATH"
for theme in $THEMES; do
    echo -n "Building release jar for $theme... "
    THEME_PATH="$(pwd)/recipes-$theme"
  
    (
        cd "$THEME_PATH"
	for proj in . ../android-support-v7-appcompat ../google-play-services_lib; do
		android update project -p $proj > /dev/null
	done
        ant clean release -Dkey.store="$SRC_PATH/release.keystore" -Dkey.alias=recipes -Dkey.store.password=qwerty -Dkey.alias.password=qwerty > build.log
        cp bin/Recipes$(tr '[:lower:]' '[:upper:]' <<< ${theme:0:1})${theme:1}-release.apk "$BIN_OUT_PATH/Recipes$(tr '[:lower:]' '[:upper:]' <<< ${theme:0:1})${theme:1}.apk"
    )
  
    echo "done"
done
clean_artifacts
)


(
    echo -n "creating distribution archive... "
    cd "$BASE_OUT_PATH/.."
    rm -rf `find "$BASE_OUT_PATH" -name "*.log"`
    rm -f "$(basename $BASE_OUT_PATH).zip"
    rm -f documentation.zip
    zip -r9 "$(basename $BASE_OUT_PATH).zip" "$(basename $BASE_OUT_PATH)" > /dev/null
    zip -r9 documentation.zip "$(basename $BASE_OUT_PATH)/documentation" "$(basename $BASE_OUT_PATH)/documentation.pdf" > /dev/null
    echo "done"
)

echo
echo "You're all set up, the generated distro is located at $BASE_OUT_PATH"

