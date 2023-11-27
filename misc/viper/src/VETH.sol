// SPDX-License-Identifier: MIT
pragma solidity ^0.8.7;

import "@openzeppelin/contracts@4.0.0/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts@4.0.0/access/Ownable.sol";

contract VETH is ERC20, Ownable {
    constructor() ERC20("VETH", "vETH") Ownable() {}

    function mint(address to, uint256 amount) public onlyOwner {
        _mint(to, amount);
    }
}
