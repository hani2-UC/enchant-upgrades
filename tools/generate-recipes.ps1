$ErrorActionPreference = 'Stop'
$recipeDirectory = Join-Path $PSScriptRoot '..\src\main\resources\data\enchant_upgrades\recipes'
New-Item -ItemType Directory -Force -Path $recipeDirectory | Out-Null
# enchantment, material, first material count, count increase, first XP level cost,
# triangular XP increase, first shelf requirement, shelves per additional level
$recipes = @'
protection,iron_ingot,2,2,3,3,0,3
fire_protection,magma_cream,2,2,3,3,0,3
feather_falling,feather,4,4,3,3,0,3
blast_protection,gunpowder,4,4,3,3,0,3
projectile_protection,flint,4,4,3,3,0,3
respiration,nautilus_shell,1,1,5,4,0,3
aqua_affinity,prismarine_crystals,4,0,6,0,3,0
thorns,cactus,4,4,6,5,0,3
depth_strider,prismarine_shard,4,4,5,4,0,3
frost_walker,packed_ice,4,4,8,6,6,6
soul_speed,soul_sand,4,4,8,6,6,3
swift_sneak,echo_shard,1,1,10,8,9,3
sharpness,blaze_rod,1,1,3,3,0,3
smite,rotten_flesh,8,8,3,3,0,3
bane_of_arthropods,spider_eye,2,2,3,3,0,3
knockback,slime_ball,2,2,3,3,0,3
fire_aspect,blaze_powder,4,4,6,5,3,3
looting,emerald,2,2,6,5,3,3
sweeping,quartz,4,4,4,3,0,3
efficiency,redstone,4,4,3,3,0,3
silk_touch,diamond,2,0,18,0,9,0
unbreaking,amethyst_shard,4,4,4,3,0,3
fortune,emerald,3,3,7,6,3,3
power,flint,2,2,3,3,0,3
punch,slime_ball,3,3,4,4,0,3
flame,fire_charge,2,0,12,0,6,0
infinity,ender_eye,2,0,24,0,12,0
luck_of_the_sea,prismarine_crystals,4,4,4,4,0,3
lure,string,4,4,3,3,0,3
loyalty,ender_pearl,2,2,4,4,0,3
impaling,prismarine_shard,4,4,3,3,0,3
riptide,heart_of_the_sea,1,0,8,6,3,3
channeling,lightning_rod,1,0,18,0,9,0
multishot,arrow,16,0,12,0,6,0
piercing,iron_nugget,4,4,3,3,0,3
quick_charge,redstone,8,8,5,4,0,3
mending,echo_shard,2,0,30,0,15,0
'@
foreach ($line in $recipes.Trim() -split '\r?\n') {
    $p = $line.Split(',')
    [ordered]@{
        type = 'enchant_upgrades:upgrade'
        enchantment = 'minecraft:' + $p[0]
        material = @{ item = 'minecraft:' + $p[1] }
        base_material = [int]$p[2]
        material_step = [int]$p[3]
        base_levels = [int]$p[4]
        level_step = [int]$p[5]
        base_shelves = [int]$p[6]
        shelf_step = [int]$p[7]
    } | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $recipeDirectory ($p[0] + '.json')) -Encoding utf8
}
Write-Output ('Generated ' + ($recipes.Trim() -split '\r?\n').Count + ' upgrade recipes.')
