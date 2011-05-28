#!/bin/sh
javah -classpath "${BUILT_PRODUCTS_DIR}/TabletWrapper.jar" -force -d "${BUILT_PRODUCTS_DIR}/Headers" "com.jhlabs.jnitablet.TabletWrapper"
