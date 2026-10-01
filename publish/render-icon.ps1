$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$book = [Drawing.Image]::FromFile((Join-Path $PSScriptRoot 'minecraft-enchanted-book.png'))
$bitmap = [Drawing.Bitmap]::new(512,512)
$graphics = [Drawing.Graphics]::FromImage($bitmap)
$graphics.Clear([Drawing.ColorTranslator]::FromHtml('#182133'))
$graphics.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
$graphics.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::Half
$graphics.DrawImage($book,[Drawing.Rectangle]::new(112,52,288,288),0,0,16,16,[Drawing.GraphicsUnit]::Pixel)
$font = [Drawing.Font]::new('Arial',36,[Drawing.FontStyle]::Bold,[Drawing.GraphicsUnit]::Pixel)
$format = [Drawing.StringFormat]::new()
$format.Alignment = [Drawing.StringAlignment]::Center
$format.LineAlignment = [Drawing.StringAlignment]::Center
$brush = [Drawing.SolidBrush]::new([Drawing.Color]::White)
$graphics.TextRenderingHint = [Drawing.Text.TextRenderingHint]::AntiAliasGridFit
$graphics.DrawString('Enchant Upgrades',$font,$brush,[Drawing.RectangleF]::new(16,357,480,96),$format)
$bitmap.Save((Join-Path $PSScriptRoot 'icon.png'),[Drawing.Imaging.ImageFormat]::Png)
$format.Dispose(); $font.Dispose(); $brush.Dispose(); $graphics.Dispose(); $bitmap.Dispose(); $book.Dispose()
