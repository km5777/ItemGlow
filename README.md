<div align="center">
  <img src="docs/images/logo.png" width="250" alt="Item Glow Logo" style="border-radius: 20px;"/>
  <h1>Item Glow</h1>
  <p>A functional and highly customizable item HUD for Minecraft 1.21.1</p>

  <p>
    <a href="https://modrinth.com/mod/item-glow-(java)"><img src="https://img.shields.io/badge/Modrinth-00AF5C?style=for-the-badge&logo=modrinth&logoColor=white" /></a>
    <a href="https://www.curseforge.com/minecraft/mc-mods/item-glow"><img src="https://img.shields.io/badge/CurseForge-F16436?style=for-the-badge&logo=curseforge&logoColor=white" /></a>
    <a href="https://github.com/km5777/ItemGlow"><img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" /></a>
    <a href="https://km5777.github.io/ItemGlow/"><img src="https://img.shields.io/badge/Documentation-6366f1?style=for-the-badge&logo=googledocs&logoColor=white" /></a>
  </p>
</div>

---

### What is Item Glow?
Item Glow is a client-side mod that displays essential information about the items in your hand directly on your HUD. Instead of opening your inventory to check durability or enchantment levels, I've made it so you can see everything at a glance with smooth animations and a clean design.

---

### Key Features

<table width="100%" border="0" cellspacing="0" cellpadding="0" style="border: none; border-collapse: collapse;">
  <tr>
    <td width="55%" align="center" valign="middle" style="border: none;">
      <img src="docs/images/netherritesword.png" width="450" style="border-radius: 20px;"/>
    </td>
    <td width="45%" valign="middle" style="padding-left: 20px; border: none;">
      <h4>🛡️ Combat Info & Durability</h4>
      <p>See exactly how many uses are left on your tools and armor. I've included a color-coded bar and numerical values that update in real-time as you use your gear.</p>
    </td>
  </tr>
</table>

<br/>

<table width="100%" border="0" cellspacing="0" cellpadding="0" style="border: none; border-collapse: collapse;">
  <tr>
    <td width="45%" valign="middle" style="padding-right: 20px; border: none;">
      <h4>📜 Enchantment & Effect Cycling</h4>
      <p>If an item has multiple enchantments or potion effects, the HUD cycles through them automatically. It keeps the screen uncluttered while making sure you know exactly what you're holding.</p>
    </td>
    <td width="55%" align="center" valign="middle" style="border: none;">
      <img src="docs/images/enchantbookpill.png" width="450" style="border-radius: 20px;"/>
    </td>
  </tr>
</table>

<br/>

<table width="100%" border="0" cellspacing="0" cellpadding="0" style="border: none; border-collapse: collapse;">
  <tr>
    <td width="55%" align="center" valign="middle" style="border: none;">
      <img src="docs/images/goldapple.png" width="450" style="border-radius: 20px;"/>
    </td>
    <td width="45%" valign="middle" style="padding-left: 20px; border: none;">
      <h4>🍎 Food & Utility Data</h4>
      <p>Holding food? You'll see nutrition and saturation values. Holding a painting? You'll see its size and variant before placing it. I've added support for many special items like Golden Apples and Suspicious Stews.</p>
    </td>
  </tr>
</table>

---

### Customization
I wanted this mod to fit any UI style, so almost everything is configurable via **Cloth Config** and **Mod Menu**:

- **Positioning:** Move the HUD to any corner or center it.
- **Appearance:** Adjust scale, background transparency, and text colors.
- **Animations:** Custom slide and fade effects for item switching.
- **Visibility:** Toggle individual elements on or off to suit your needs.

---

### For Developers
I've designed Item Glow to be a platform for other modders. If you're building a mod and want your custom item data to show up in my HUD, you can use the built-in provider system.

#### Integration API
To add your own data to the HUD, simply add a new provider to the `ItemGlowApi.PROVIDERS` list. Here's how you can do it:

```java
// Registering a custom data provider
ItemGlowApi.PROVIDERS.add(stack -> {
    if (stack.getItem() instanceof ManaWand) {
        return List.of(new ItemGlowApi.TooltipLine(
            Text.literal("Mana: 50/100"), 
            0x55FFFF, // Color
            100       // Priority
        ));
    }
    return null;
});
```

#### Why Integrate?
<table width="100%" border="0" cellspacing="0" cellpadding="0" style="border: none; border-collapse: collapse;">
  <tr>
    <td width="45%" valign="middle" style="padding-right: 20px; border: none;">
      <ul>
        <li><b>Zero UI Conflict:</b> Your data renders in a style consistent with the rest of the HUD.</li>
        <li><b>User Preference:</b> Users can scale, move, and toggle your data just like native elements.</li>
        <li><b>Efficiency:</b> I've optimized the rendering engine to ensure minimal impact on performance.</li>
      </ul>
    </td>
    <td width="55%" align="center" valign="middle" style="border: none;">
      <img src="docs/images/manabar.png" width="450" style="border-radius: 20px;"/>
    </td>
  </tr>
</table>

Full details are available in my [API Documentation](https://km5777.github.io/ItemGlow/integration.html).

---

### Requirements
- **Fabric Loader** (0.15.11 or newer)
- **Fabric API**
- **Cloth Config API** (Required for the config screen)
- **Mod Menu** (Recommended for easy access to settings)

### Installation
1. Get the latest `.jar` from [Modrinth](https://modrinth.com/mod/item-glow-(java)) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/item-glow).
2. Drop it into your `mods` folder.
3. Start the game and configure it via Mod Menu.

---
<div align="center">
  <p>Support the project: <a href="https://www.paypal.com/donate?token=elNZNapST-z28m88rwdBxMGMxu9C6HetcvJuNdJtdSLSRm9uwXEztr6x9mjz3IVpQAAoOzdnhNLRsZ26&locale.x=US">Donate via PayPal</a></p>
  <sub>Created by <a href="https://github.com/km5777">K_M577</a></sub>
</div>
