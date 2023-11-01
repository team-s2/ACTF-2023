#pragma once

#include <m4ri/m4ri.h>
#include <vector>
#include <stdexcept>
#include <bitset>
#include <memory>

namespace CCSolver {

template<size_t vars>
struct Allocator {
    size_t cur = 0;
    size_t alloc() {
        if (cur >= vars) {
            puts("bad var alloc");
            exit(1);
        }
        return cur++;
    }
};

// BitVar is COW bitset
template <size_t count>
struct BitVar {
    using Self = BitVar<count>;
    using BitSetType = std::bitset<count>;
    std::shared_ptr<BitSetType> bv;
    static BitVar<count> zero;
    BitVar() : bv(zero.bv) {}
    BitVar(std::shared_ptr<BitSetType> &&bv) : bv(std::move(bv)) {}
    BitVar(const Self& other) : bv(other.bv) {}
    BitVar(Self&& other) : bv(std::move(other.bv)) {}
    Self& operator = (const Self& other) {
        bv = other.bv;
        return *this;
    }
    Self& operator = (Self&& other) {
        bv = std::move(other.bv);
        return *this;
    }
    Self& operator = (const BitSetType& other) {
        if (is_shared()) {
            bv = std::make_shared<BitSetType>(other);
        } else {
            *bv = other;
        }
        return *this;
    }

    bool is_shared() {
        return bv.use_count() > 1;
    }
    void new_if_shared() {
        if (is_shared()) {
            bv = std::make_shared<BitSetType>(*bv);
        }
    }

    void reset() {
        bv = zero.bv;
    }
    void set() {
        new_if_shared();
        bv->set();
    }
    void flip() {
        new_if_shared();
        bv->flip();
    }
    void reset(size_t x) {
        new_if_shared();
        bv->reset(x);
    }
    void set(size_t x) {
        new_if_shared();
        bv->set(x);
    }
    void flip(size_t x) {
        new_if_shared();
        bv->flip(x);
    }

    bool operator[](size_t x) const {
        return (*bv)[x];
    }

    Self operator ^ (const Self& other) {
        return *bv ^ *other.bv;
    }
    Self& operator ^= (const Self& other) {
        new_if_shared();
        *bv ^= *other.bv;
        return *this;
    }
};

template <size_t count>
BitVar<count> BitVar<count>::zero(std::make_shared<BitSetType>());

template<size_t bit_cnt, size_t vars>
struct BitVec {
    static constexpr size_t bit_cnt_value = bit_cnt;
    static constexpr size_t vars_value = vars;
    using SelfBitVar = BitVar<vars + 1>;
    using Self = BitVec<bit_cnt, vars>;
    SelfBitVar bits[bit_cnt];

    BitVec() {}
    BitVec(Allocator<vars>& allocator) {
        alloc(allocator);
    }
    BitVec(size_t v) {
        set(v);
    }
    BitVec(const Self &v) {
        for (int i = 0; i < bit_cnt; i++) {
            bits[i] = v.bits[i];
        }
    }

    void set_zero() {
        for (int i = 0; i < bit_cnt; i++) {
            bits[i].reset();
        }
    }

    void set(size_t v) {
        for (int i = 0; i < bit_cnt; i++) {
            bits[i].reset();
            if ((v >> i) & 1) {
                bits[i].set(0);
            }
        }
    }

    template<size_t bit_cnt_inner>
    void set(const BitVec<bit_cnt_inner, vars> &v) {
        for (int i = 0; i < std::min(bit_cnt_inner, bit_cnt); i++) {
            bits[i] = v.bits[i];
        }
    }

    template<typename Settable>
    Self &operator = (const Settable &rhs) {
        set(rhs);
        return *this;
    }

    template<size_t left, size_t right>
    BitVec<right - left + 1, vars> trunc() const {
        BitVec<right - left + 1, vars> ret;
        for (int i = 0; i < right - left + 1; i++) {
            ret.bits[i] = bits[left + i];
        }
        return ret;
    }

    void alloc(Allocator<vars>& allocator, size_t idx) {
        bits[idx].reset();
        bits[idx].set(allocator.alloc() + 1);
    }
    void alloc(Allocator<vars>& allocator) {
        for (size_t i = 0; i < bit_cnt; i++) {
            alloc(allocator, i);
        }
    }

    Self &operator ^= (const Self &rhs) {
        for (size_t i = 0; i < bit_cnt; i++) {
            bits[i] ^= rhs.bits[i];
        }
        return *this;
    }

    Self operator ^ (const Self &rhs) const {
        Self ret = *this;
        ret ^= rhs;
        return ret;
    }

    Self &operator ^= (size_t rhs) {
        for (size_t i = 0; i < bit_cnt; i++) {
            if ((rhs >> i) & 1) bits[i].flip(0);
        }
        return *this;
    }

    Self operator ^ (size_t rhs) const {
        Self ret = *this;
        ret ^= rhs;
        return ret;
    }

    Self &operator &= (size_t rhs) {
        for (size_t i = 0; i < bit_cnt; i++) {
            if ((rhs >> i) & 1) continue;
            bits[i].reset();
        }
        return *this;
    }

    Self operator & (size_t rhs) const {
        Self ret = *this;
        ret &= rhs;
        return ret;
    }

    Self &operator <<= (ssize_t cnt) {
        if (cnt < 0) {
            return *this >>= -cnt;
        }
        for (ssize_t i = (ssize_t)bit_cnt - 1; i >= cnt; i--) {
            bits[i] = bits[i - cnt];
        }
        for (ssize_t i = 0; i < std::min(cnt, (ssize_t)bit_cnt); i++) {
            bits[i].reset();
        }
        return *this;
    }
    Self operator << (ssize_t cnt) const {
        Self ret = *this;
        ret <<= cnt;
        return ret;
    }

    Self &operator >>= (ssize_t cnt) {
        if (cnt < 0) {
            return *this <<= -cnt;
        }
        for (ssize_t i = cnt; i < (ssize_t)bit_cnt; i++) {
            bits[i - cnt] = bits[i];
        }
        for (ssize_t i = std::max((ssize_t)bit_cnt - cnt, (ssize_t)0); i < (ssize_t)bit_cnt; i++) {
            bits[i].reset();
        }
        return *this;
    }
    Self operator >> (ssize_t cnt) const {
        Self ret = *this;
        ret >>= cnt;
        return ret;
    }

    template <size_t v>
    void spread(const SelfBitVar &bit) {
        for (size_t i = 0; i < bit_cnt; i++) {
            if ((v >> i) & 1) {
                bits[i] ^= bit;
            }
        }
    }
	
    void spread(const SelfBitVar &bit, size_t v) {
        for (size_t i = 0; i < bit_cnt; i++) {
            if ((v >> i) & 1) {
                bits[i] ^= bit;
            }
        }
    }

    template <size_t mod>
    void gf_mul2() {
        SelfBitVar high = bits[bit_cnt - 1];
        *this <<= 1;
        spread<mod>(high);
    }

    template <size_t mod, size_t v>
    Self gf_mul() const {
        Self ret;
        ret.set_zero();
        Self tmp = *this;
        size_t value = v;
        while (value) {
            if (value & 1) {
                ret ^= tmp;
            }
            tmp.gf_mul2<mod>();
            value >>= 1;
        }
        return ret;
    }
};

template<typename T>
struct IsBitVec {
    static constexpr bool value = false;
};

template<size_t bit_cnt, size_t vars>
struct IsBitVec<BitVec<bit_cnt, vars>> {
    static constexpr bool value = true;
};

template <size_t left, size_t right, typename T, std::enable_if_t<IsBitVec<T>::value, bool> = true>
BitVec<right - left + 1, T::vars_value> trunc(const T &x) {
    return x.template trunc<left, right>();
}

template<size_t left, size_t right, typename T, std::enable_if_t<std::is_integral<T>::value, bool> = true>
constexpr size_t trunc(const T &x) {
    return (x >> left) & ((1ull << (right - left + 1)) - 1);
}

template <typename T, std::enable_if_t<std::is_integral<T>::value, bool> = true>
void spread(T &x, const size_t &bit, size_t v) {
    if (bit) {
        x ^= v;
    }
}

template <typename T, std::enable_if_t<IsBitVec<T>::value, bool> = true>
void spread(T &x, const typename T::SelfBitVar &bit, size_t v) {
    return x.spread(bit, v);
}

template <size_t v, typename T, std::enable_if_t<std::is_integral<T>::value, bool> = true>
void spread(T &x, const size_t &bit) {
    if (bit) {
        x ^= v;
    }
}

template <size_t v, typename T, std::enable_if_t<IsBitVec<T>::value, bool> = true>
void spread(T &x, const typename T::SelfBitVar &bit) {
    return x.template spread<v>(bit);
}

template <typename T, std::enable_if_t<IsBitVec<T>::value, bool> = true>
typename T::SelfBitVar get_bit(const T &x, size_t index) {
    return x.bits[index];
}

template<typename T, std::enable_if_t<std::is_integral<T>::value, bool> = true>
constexpr size_t get_bit(const T &x, size_t index) {
    return (x >> index) & 1;
}

template<typename T, std::enable_if_t<std::is_integral<T>::value, bool> = true>
constexpr T reverse_order(const T &x) {
    T ret = 0;
    for (size_t i = 0; i < sizeof(x) * 8; i++) {
        ret |= ((x >> (sizeof(x) * 8 - i - 1)) & 1) << i;
    }
    return ret;
}

template<size_t mod, size_t v, typename T, std::enable_if_t<std::is_integral<T>::value, bool> = true>
T gf_mul(const T &x) {
    T ret = 0, tmp = x;
    size_t value = v;
    while (value) {
        if (value & 1) {
            ret ^= tmp;
        }
        bool high = tmp & ((T)(1) << (sizeof(x) * 8 - 1));
        tmp <<= 1;
        if (high) {
            tmp ^= mod;
        }
        value >>= 1;
    }
    return ret;
}

template<typename T, std::enable_if_t<IsBitVec<T>::value, bool> = true>
T reverse_order(const T &x) {
    T ret;
    for (size_t i = 0; i < T::bit_cnt_value; i++) {
        ret.bits[T::bit_cnt_value - i - 1] = x.bits[i];
    }
    return ret;
}

template <size_t mod, size_t v, typename T, std::enable_if_t<IsBitVec<T>::value, bool> = true>
T gf_mul(const T &x) {
    return x.template gf_mul<mod, v>();
}

struct Matrix {
    mzd_t *matrix;
    Matrix(size_t rows, size_t cols) {
        matrix = mzd_init(rows, cols);
    }
    Matrix(mzd_t *matrix) : matrix(matrix) {}
    Matrix(const Matrix &other) {
        matrix = mzd_copy(nullptr, other.matrix);
    }
    Matrix(Matrix &&other) : matrix(other.matrix) {
        other.matrix = nullptr;
    }
    ~Matrix() {
        if (matrix) mzd_free(matrix);
    }
    Matrix &operator=(const Matrix &other) {
        if (matrix) mzd_free(matrix);
        matrix = mzd_copy(nullptr, other.matrix);
        return *this;
    }
    Matrix &operator=(Matrix &&other) {
        if (matrix) mzd_free(matrix);
        matrix = other.matrix;
        other.matrix = nullptr;
        return *this;
    }
    struct BitAccessor {
        mzd_t *matrix;
        size_t row, col;
        BitAccessor(mzd_t *matrix, size_t row, size_t col) : matrix(matrix), row(row), col(col) {}
        operator bool() const {
            return mzd_read_bit(matrix, row, col);
        }
        BitAccessor & operator=(bool __x) _GLIBCXX_NOEXCEPT {
            mzd_write_bit(matrix, row, col, __x);
            return *this;
        }
        BitAccessor &operator=(const BitAccessor &__x) _GLIBCXX_NOEXCEPT { return *this = bool(__x); }
        bool operator==(const BitAccessor &__x) const { return bool(*this) == bool(__x); }
        bool operator<(const BitAccessor &__x) const { return !bool(*this) && bool(__x); }
    };
    BitAccessor operator()(size_t row, size_t col) {
        return BitAccessor(matrix, row, col);
    }
    bool operator()(size_t row, size_t col) const {
        return mzd_read_bit(matrix, row, col);
    }
    Matrix transpose() const {
        return Matrix(mzd_transpose(nullptr, matrix));
    }
    void print() {
        mzd_print(matrix);
    }
    Matrix solve(const Matrix &B) const {
        Matrix ret(B);
        int not_ok = mzd_solve_left(matrix, ret.matrix, 0, false);
        if (not_ok) {
            throw std::runtime_error("no solution");
        }
        return ret;
    }
    template<size_t n>
    void copy_row(const size_t row, const std::bitset<n> &bs) {
        assert(n == matrix->ncols);
        for (size_t i = 0; i < n; i++) {
            mzd_write_bit(matrix, row, i, bs[i]);
        }
    }
};

template<size_t vars>
struct Solver {
    using Self = Solver;
    Matrix A, b;
    Matrix solution;
    size_t cnt, cond_cnt;
    Solver(size_t conds=vars) : A(conds, vars), b(std::max(conds, vars), 1), cnt(0), solution(nullptr), cond_cnt(conds) {}
    void add(const BitVar<vars + 1> &conds, size_t equ) {
        if (cnt >= cond_cnt) {
            puts("too many conds");
            exit(1);
        }
        for (size_t i = 1; i <= vars; i++) {
            A(cnt, i - 1) = conds[i];
        }
        b(cnt, 0) = conds[0] ^ equ;
        cnt++;
    }
    template<size_t bit_cnt>
    void add(const BitVec<bit_cnt, vars> &conds, size_t equ) {
        for (size_t i = 0; i < bit_cnt; i++) {
            add(conds.bits[i], (equ >> i) & 1);
        }
    }
    Matrix solve() {
        if (cnt != cond_cnt) {
            puts("conds is not equal");
            exit(1);
        }
        return solution = A.solve(b);
    }
    size_t eval(const BitVar<vars + 1> &var) const {
        if (solution.matrix == nullptr) throw std::runtime_error("no solution yet");
        size_t ret = var[0];
        for (size_t i = 1; i <= vars; i++) {
            ret ^= var[i] && solution(i - 1, 0);
        }
        return ret;
    }
    template<size_t bit_cnt>
    size_t eval(const BitVec<bit_cnt, vars> &variable) const {
        size_t ret = 0;
        for (size_t i = 0; i < bit_cnt; i++) {
            ret ^= eval(variable.bits[i]) << i;
        }
        return ret;
    }
    template<typename Evalable>
    size_t operator()(const Evalable &v) const {
        return eval(v);
    }
    template<typename Evalable>
    size_t operator[](const Evalable &v) const {
        return eval(v);
    }
};

}