set "OPTIONS=-charset charset.txt -type mtsdf -size 48 -pxrange 12 -padding 6 -format png"

msdf-atlas-gen.exe %OPTIONS% -font Inter-Regular-kern.ttf -imageout output/inter-regular.png -json output/inter-regular.json
msdf-atlas-gen.exe %OPTIONS% -font Inter-Bold-kern.ttf -imageout output/inter-bold.png -json output/inter-bold.json
