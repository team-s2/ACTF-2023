// Example of using CCSolver to solve ACTF2023 mt_speed_run
// Install dependency: sudo apt install libm4ri-dev libfmt-dev
// Compile: g++ -std=c++17 -O3 -o mtsolver -march=native mtsolver.cpp `pkg-config --cflags --libs m4ri` `pkg-config --cflags --libs fmt`
// Run: ./mtsolver

#include "solver.hpp"
#include <random>
#include <limits>
#include <fmt/core.h>
#include <ctime>
#include <string>
#include <fstream>

template<typename ValueType>
struct MTState {
    static constexpr size_t N = 624;
    static constexpr size_t vars = 624 * sizeof(uint32_t) * 8;
    size_t magic_a, magic_b, magic_c, magic_d, magic_e;
    ValueType states[N];
    size_t cursor = N;
    void update() {
        for (size_t i = 0; i < N; i++) {
            ValueType y = (states[i] & 0x80000000) ^ (states[(i + 1) % N] & 0x7FFFFFFF);
            states[i] = states[(i + 397) % N] ^ (y >> 1);
            CCSolver::spread(states[i], CCSolver::get_bit(y, 0), magic_c);
        }
    }
    ValueType temper(ValueType state) {
        state ^= state >> 11;
        state ^= (state << 7) & magic_a;
        state ^= (state << 15) & magic_b;
        state ^= (state << magic_e) & magic_d;
        state ^= state >> 18;
        return state;
    }
    ValueType next() {
        if (cursor == N) {
            update();
            cursor = 0;
        }
        return temper(states[cursor++]);
    }
    template<size_t bit>
    ValueType next_bits() {
        static_assert(bit <= 32, "bit must be less than 32");
        return next() >> (32 - bit);
    }
    template<size_t bit>
    auto next_trunc() {
        return CCSolver::trunc<0, bit - 1>(next_bits<bit>());
    }
};

using MTStateInt = MTState<uint32_t>;
using MTStateBV = MTState< CCSolver::BitVec<sizeof(uint32_t) * 8, MTStateInt::vars> >;
using MTSolver = CCSolver::Solver<MTStateInt::vars>;
MTStateBV allocated_mt_states_bv() {
    MTStateBV state;
    CCSolver::Allocator<MTStateBV::vars> alloc;
    for (size_t i = 0; i < MTStateBV::N; i++) {
        state.states[i].alloc(alloc);
    }
    return state;
}
MTStateInt mt_state_from_solver(const MTSolver &solver, const MTStateBV &state_bv) {
    MTStateInt state;
    state.magic_a = state_bv.magic_a;
    state.magic_b = state_bv.magic_b;
    state.magic_c = state_bv.magic_c;
    state.magic_d = state_bv.magic_d;
    state.magic_e = state_bv.magic_e;
    for (size_t i = 0; i < MTStateInt::N; i++) {
        state.states[i] = solver.eval(state_bv.states[i]);
    }
    return state;
}

int main() {
    fputs("Reading...\n", stderr);
    uint32_t my_rands[30000];
    std::ifstream ifs("test_my_rands.txt", std::ios::in);
    std::string my_rands_str;
    ifs >> my_rands_str;
    if (my_rands_str.length() != 30000) {
        fputs("Invalid length of my_rands\n", stderr);
        return 1;
    }
    for (size_t i = 0; i < 30000; i++) {
        if (my_rands_str[i] == '0') {
            my_rands[i] = 0;
        } else if (my_rands_str[i] == '1') {
            my_rands[i] = 1;
        } else {
            fputs("Invalid content of my_rands\n", stderr);
            return 1;
        }
    }
    MTStateBV state = allocated_mt_states_bv();
    {
        ifs >> state.magic_a;
        ifs >> state.magic_b;
        ifs >> state.magic_c;
        ifs >> state.magic_d;
        ifs >> state.magic_e;
    }
    fputs("Building...\n", stderr);
    MTSolver solver(30000);
	state.cursor = 0;
    for (size_t i = 0; i < 30000; i++) {
        solver.add(state.next_trunc<1>(), my_rands[i]);
    }
    state.next();
    fputs("Solving...\n", stderr);
    // solver.A.print();
    // solver.b.transpose().print();
    solver.solve();
    // solver.solution.transpose().print();
    fputs("Evaluating...\n", stderr);
    MTStateInt solved_state = mt_state_from_solver(solver, state);
    std::string outputs;
    for (size_t i = 0; i < MTStateInt::N; i++) {
        fmt::format_to(std::back_inserter(outputs), "{} ", solved_state.states[i]);
    }
    fmt::print("{}\n", outputs);
}