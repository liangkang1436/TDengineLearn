cd E:\IDEAProject\TDengineLearn\TDengineDataInsert
call mvn clean package -P nexusWithoutSnapshot jib:buildTar
move target\tdengine-data-insert-image.tar C:\Users\wwwli\Desktop\tdengine-data-insert-image.tar
pause
