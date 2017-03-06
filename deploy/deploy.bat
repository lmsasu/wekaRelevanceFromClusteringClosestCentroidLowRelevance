rd /s /q ..\dist\RelevanceFromClusteringClosestCentroidLowRelevance
del ..\dist\RelevanceFromClusteringClosestCentroidLowRelevance.zip
rd /s /q "%userprofile%\wekafiles\packages\RelevanceFromClusteringClosestCentroidLowRelevance"
call ant -buildfile ..\build_package.xml make_package
xcopy /Q /Y ..\dist\RelevanceFromClusteringClosestCentroidLowRelevance "%userprofile%\wekafiles\packages\RelevanceFromClusteringClosestCentroidLowRelevance" /s /i