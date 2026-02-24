set "OPTIONS=-charset charset.txt -type mtsdf -size 48 -pxrange 12 -padding 6 -format png"

msdf-atlas-gen.exe %OPTIONS% -font font/SF-Pro-Display-Medium-kern.otf -imageout output/sf-pro-medium.png -json output/sf-pro-medium.json
msdf-atlas-gen.exe %OPTIONS% -font font/SF-Pro-Display-Semibold-kern.otf -imageout output/sf-pro-semibold.png -json output/sf-pro-semibold.json
msdf-atlas-gen.exe %OPTIONS% -font font/SF-Pro-Display-regular-kern.otf -imageout output/sf-pro-regular.png -json output/sf-pro-regular.json
