# @version ^0.2.16

from vyper.interfaces import ERC20

event Deposit:
    user: indexed(address)
    token: indexed(address)
    amount: uint256

event Withdraw:
    user: indexed(address)
    token: indexed(address)
    amount: uint256

event Swap:
    user: indexed(address)
    tokenIn: indexed(address)
    tokenOut: indexed(address)
    amount: uint256


ETH: constant(address) = 0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE
NCOINS: constant(int128) = 2
BONUS: constant(uint256) = 4276545

owner: public(address)
initialized: public(bool)
coins: public(address[NCOINS])
ratio: public(uint256)
balances: public(HashMap[int128, HashMap[address, uint256]])


@payable
@external
def __init__():
    self.owner = msg.sender
    self.initialized = False


@payable
@external
def initialize(underlying: address):
    assert msg.sender == self.owner
    assert not self.initialized, "has been initialized"
    self.coins[0] = ETH
    self.coins[1] = underlying
    self.ratio = 2
    self.initialized = True


@payable
@external
@nonreentrant('lock')
def deposit(index: int128, amount: uint256):
    assert self.initialized, "not available yet"
    assert index >= 0 and index < NCOINS
    token: address = self.coins[index]

    if token == ETH:
        assert msg.value >= amount
        if msg.value - amount > 0:
            raw_call(msg.sender, b"", value=msg.value - amount)
    else:
        ERC20(token).transferFrom(msg.sender, self, amount)
    self.balances[index][msg.sender] += amount
    
    log Deposit(msg.sender, token, amount)


@external
@nonreentrant('lock')
def withdraw(index: int128, amount: uint256):
    assert self.initialized, "not available yet"
    assert index >= 0 and index < NCOINS
    token: address = self.coins[index]
    assert self.balances[index][msg.sender] >= amount

    if token == ETH:
        raw_call(msg.sender, b"", value=amount)
    else:
        ERC20(token).transfer(msg.sender, amount)
    self.balances[index][msg.sender] -= amount
    
    log Withdraw(msg.sender, token, amount)


@payable
@external
@nonreentrant('lock')
def swap(in_index: int128, out_index: int128, amount: uint256):
    assert self.initialized, "not available yet"
    assert in_index >= 0 and in_index < NCOINS
    assert out_index >= 0 and out_index < NCOINS
    assert in_index != out_index
    
    if in_index == 0:
        if msg.value - amount > 0:
            raw_call(msg.sender, b"", value=msg.value - amount)
        increase: decimal = convert(amount, decimal) * self.ratio
        self.balances[out_index][msg.sender] += convert(increase, uint256) + BONUS
    else:
        _before: uint256 = ERC20(self.coins[in_index]).balanceOf(self)
        if msg.value > 0:
            raw_call(msg.sender, b"", value=msg.value)
        ERC20(self.coins[in_index]).transferFrom(msg.sender, self, amount)
        _after: uint256 = ERC20(self.coins[in_index]).balanceOf(self)
        increase: decimal = convert(_after - _before, decimal) / self.ratio
        self.balances[out_index][msg.sender] += convert(increase, uint256)
    
    log Swap(msg.sender, self.coins[in_index], self.coins[out_index], amount)


@external
@view
def isSolved() -> bool:
    return self.balance == 0